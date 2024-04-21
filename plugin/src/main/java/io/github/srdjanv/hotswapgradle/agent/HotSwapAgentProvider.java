package io.github.srdjanv.hotswapgradle.agent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import io.github.srdjanv.hotswapgradle.agent.githubschema.GithubApiAssetsSchema;
import io.github.srdjanv.hotswapgradle.agent.githubschema.GithubApiReleaseSchema;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.util.FileIOUtils;
import io.github.srdjanv.hotswapgradle.util.FileUtils;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.gradle.api.file.DirectoryProperty;
import org.jetbrains.annotations.Nullable;

public class HotSwapAgentProvider {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ExecutorService downloader = Executors.newFixedThreadPool(2);
    private final Future<Path> latestStable;
    private final String agentReleaseApiUrl;
    private final Future<List<GithubApiReleaseSchema>> agentsManifest;
    private final Path agentsDir;

    public HotSwapAgentProvider(String agentReleaseApiUrl, DirectoryProperty workingDirectory) {
        this.agentReleaseApiUrl = agentReleaseApiUrl;
        this.agentsDir = FileUtils.agentDir(workingDirectory).getAsFile().toPath();
        FileIOUtils.createDirs(agentsDir);
        agentsManifest = downloader.submit(initAgents(workingDirectory));
        latestStable = requestAgent(new DownloadConfig(AgentType.DEFAULT));
    }

    public Future<Path> getLatestStable() {
        return latestStable;
    }

    public Path requestAgent(DcevmSpec dcevmSpec) {
        var agentType = dcevmSpec.getAgentType().get();
        var snapshot = dcevmSpec.getUseSnapshot();
        String gitTag = dcevmSpec.getGitTagName().getOrNull();
        var config = new DownloadConfig(AgentType.valueOf(agentType.toUpperCase()), snapshot.get(), gitTag);
        try {
            return requestAgent(config).get(10, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public Future<Path> requestAgent(DownloadConfig config) {
        return downloader.submit(() -> {
            GithubApiReleaseSchema releaseSchema = getReleaseSchema(config);
            GithubApiAssetsSchema assetsSchema = getAssetSchema(releaseSchema, config);
            Path local = findLocally(assetsSchema);
            if (local != null) return local;
            return doDownload(assetsSchema);
        });
    }

    private GithubApiAssetsSchema getAssetSchema(GithubApiReleaseSchema releaseSchema, DownloadConfig config) {
        for (GithubApiAssetsSchema asset : releaseSchema.assets()) {
            if (asset.isValidJar(config.agentType())) return asset;
        }
        throw new InvalidDownloadRequestException("GithubApiAssetsSchema is not resolvable. Schema: " + releaseSchema);
    }

    private GithubApiReleaseSchema getReleaseSchema(DownloadConfig config) {
        GithubApiReleaseSchema assets = null;
        try {
            List<GithubApiReleaseSchema> manifests = agentsManifest.get(10, TimeUnit.MINUTES);
            if (config.tagName() != null)
                assets = manifests.stream()
                        .filter(m -> m.tag_name().equals(config.tagName()))
                        .findFirst()
                        .orElse(null);

            if (assets == null)
                for (GithubApiReleaseSchema a : manifests) {
                    if (a.prerelease() && config.snapshot()) {
                        assets = a;
                        break;
                    }

                    if (!a.prerelease()) {
                        assets = a;
                        break;
                    }
                }

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        if (assets != null) return assets;
        throw new InvalidDownloadRequestException(
                "GithubApiReleaseSchema is not resolvable, DownloadConfig: " + config);
    }

    private Path doDownload(GithubApiAssetsSchema schema) {
        Path downloadPath = agentsDir.resolve(schema.name());
        boolean success = false;
        for (int errors = 0; errors < 4; errors++) {
            try (BufferedInputStream in = new BufferedInputStream(new URL(schema.browser_download_url()).openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(downloadPath.toFile())) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                success = true;
            } catch (IOException e) {
                // todo
                e.printStackTrace();
            }
        }
        if (!success) throw new DownloadException(String.format("Agent download failed, Path: %s", downloadPath));
        return downloadPath;
    }

    @Nullable
    private Path findLocally(GithubApiAssetsSchema schema) {
        Path agent = agentsDir.resolve(schema.name());
        if (Files.exists(agent)) return agent;
        return null;
    }

    private Callable<List<GithubApiReleaseSchema>> initAgents(DirectoryProperty workingDir) {
        return () -> {
            List<GithubApiReleaseSchema> manifest = new ArrayList<>();
            List<GithubApiReleaseSchema> loadedAgentsManifest = loadAgentsManifest(gson, workingDir);
            List<GithubApiReleaseSchema> downloadRemoteAgentsManifest =
                    downloadRemoteAgentsManifest(gson, agentReleaseApiUrl);

            if (!loadedAgentsManifest.equals(downloadRemoteAgentsManifest)) {
                manifest.addAll(downloadRemoteAgentsManifest);
            } else {
                manifest.addAll(downloadRemoteAgentsManifest);
                saveAgentsManifest(gson, workingDir, downloadRemoteAgentsManifest);
            }

            manifest.sort(Comparator.comparing(GithubApiReleaseSchema::created_at)
                    .thenComparing(GithubApiReleaseSchema::published_at)
                    .reversed());
            return manifest;
        };
    }

    private static void saveAgentsManifest(
            Gson gson, DirectoryProperty workingDir, List<GithubApiReleaseSchema> manifest) {
        try {
            var text = gson.toJson(manifest);
            FileIOUtils.saveStringToFile(text, FileUtils.agentVersion(workingDir));
        } catch (JsonParseException | UncheckedIOException exception) {
            exception.printStackTrace();
        }
    }

    private static List<GithubApiReleaseSchema> loadAgentsManifest(Gson gson, DirectoryProperty workingDir) {
        List<GithubApiReleaseSchema> agents = Collections.emptyList();
        try {
            var rawAgents = FileIOUtils.loadTextFromFile(FileUtils.agentVersion(workingDir));
            agents = gson.fromJson(rawAgents, new TypeToken<List<GithubApiReleaseSchema>>() {}.getType());
        } catch (JsonParseException | UncheckedIOException ignored) {
        } finally {
            if (agents == null) agents = Collections.emptyList();
        }
        return agents;
    }

    private static List<GithubApiReleaseSchema> downloadRemoteAgentsManifest(Gson gson, String agentReleaseApiUrl) {
        List<GithubApiReleaseSchema> agentsManifest = Collections.emptyList();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            var getMethod = new HttpGet(agentReleaseApiUrl);
            var response = httpClient.execute(getMethod);
            agentsManifest = gson.fromJson(
                    new JsonReader(new InputStreamReader(response.getEntity().getContent())),
                    new TypeToken<List<GithubApiReleaseSchema>>() {}.getType());
        } catch (IOException e) {
            System.err.printf("Couldn't load the release URL: %s%n", agentsManifest);
            e.printStackTrace();
        } finally {
            if (agentsManifest == null) agentsManifest = Collections.emptyList();
        }

        return agentsManifest;
    }

    public static class DownloadException extends RuntimeException {
        public DownloadException() {
            super();
        }

        public DownloadException(String message) {
            super(message);
        }

        public DownloadException(String message, Throwable cause) {
            super(message, cause);
        }

        public DownloadException(Throwable cause) {
            super(cause);
        }
    }

    public static class InvalidDownloadRequestException extends DownloadException {
        public InvalidDownloadRequestException() {
            super();
        }

        public InvalidDownloadRequestException(String message) {
            super(message);
        }

        public InvalidDownloadRequestException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidDownloadRequestException(Throwable cause) {
            super(cause);
        }
    }
}
