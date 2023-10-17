package micdoodle8.mods.galacticraft.core.asm;

import static micdoodle8.mods.galacticraft.core.asm.GCLoadingPlugin.isObf;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * This cannot be a mixin, as it'd otherwise break on thermos server
 * <p>
 * Scrubs ServerConfigurationManager for new EntityPlayerMP and replace it with new GCEntityPlayerMP
 */
public class GCTransformer implements IClassTransformer, Opcodes {

    private static final String REPLACEMENT_CLASS_INTERNAL_NAME = "micdoodle8/mods/galacticraft/core/entities/player/GCEntityPlayerMP";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if ("net.minecraft.server.management.ServerConfigurationManager".equals(transformedName)) {
            return transform(basicClass);
        }
        return basicClass;
    }

    private static byte[] transform(byte[] basicClass) {
        final ClassReader classReader = new ClassReader(basicClass);
        final ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        for (MethodNode mn : classNode.methods) {
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (isTargetTypeNode(node)) {
                    ((TypeInsnNode) node).desc = REPLACEMENT_CLASS_INTERNAL_NAME;
                } else if (isTargetMethodNode(node)) {
                    ((MethodInsnNode) node).owner = REPLACEMENT_CLASS_INTERNAL_NAME;
                }
            }
        }
        final ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static boolean isTargetMethodNode(AbstractInsnNode node) {
        return node instanceof MethodInsnNode && node.getOpcode() == INVOKESPECIAL
                && ((MethodInsnNode) node).name.equals("<init>")
                && ((MethodInsnNode) node).owner.equals(isObf ? "mw" : "net/minecraft/entity/player/EntityPlayerMP");
    }

    private static boolean isTargetTypeNode(AbstractInsnNode node) {
        return node instanceof TypeInsnNode && node.getOpcode() == NEW
                && ((TypeInsnNode) node).desc.equals(isObf ? "mw" : "net/minecraft/entity/player/EntityPlayerMP");
    }
}
