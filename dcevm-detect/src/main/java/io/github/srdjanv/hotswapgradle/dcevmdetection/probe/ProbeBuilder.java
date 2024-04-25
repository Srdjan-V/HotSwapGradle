package io.github.srdjanv.hotswapgradle.dcevmdetection.probe;

import static org.objectweb.asm.Opcodes.*;

import com.google.common.base.Suppliers;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

// This is taken from Gradle internals
public class ProbeBuilder {
    public static final String MARKER_PREFIX = "DCEVM_DETECT_PROBE_VALUE:";

    public static void writeClass(Path probeFile) throws IOException {
        Files.createFile(probeFile);
        try (var stream = new FileOutputStream(probeFile.toFile())) {
            stream.write(probeClass.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Supplier<byte[]> probeClass = Suppliers.memoize(ProbeBuilder::createProbeClass);

    private static byte[] createProbeClass() {
        ClassWriter cw = new ClassWriter(0);
        createClassHeader(cw);
        createConstructor(cw);
        createMainMethod(cw);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void createClassHeader(ClassWriter cw) {
        cw.visit(V1_1, ACC_PUBLIC + ACC_SUPER, "JavaProbe", null, "java/lang/Object", null);
    }

    private static void createMainMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        for (Props type : Props.values()) {
            dumpProperty(mv, type.getValue());
        }
        mv.visitInsn(RETURN);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLocalVariable("args", "[Ljava/lang/String;", null, l0, l3, 0);
        mv.visitMaxs(3, 1);
        mv.visitEnd();
    }

    private static void dumpProperty(MethodVisitor mv, String property) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(MARKER_PREFIX);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);

        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(property);
        mv.visitLdcInsn("unknown");
        mv.visitMethodInsn(
                INVOKESTATIC,
                "java/lang/System",
                "getProperty",
                "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    private static void createConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", "LJavaProbe;", null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
