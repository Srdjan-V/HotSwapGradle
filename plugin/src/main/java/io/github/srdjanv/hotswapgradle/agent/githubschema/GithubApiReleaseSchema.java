package io.github.srdjanv.hotswapgradle.agent.githubschema;

import com.github.bsideup.jabel.Desugar;

import java.util.Date;
import java.util.List;

@Desugar
public record GithubApiReleaseSchema(
        String name,
        String tag_name,
        boolean prerelease,
        int id,
        Date created_at,
        Date published_at,
        List<GithubApiAssetsSchema> assets) {
}
