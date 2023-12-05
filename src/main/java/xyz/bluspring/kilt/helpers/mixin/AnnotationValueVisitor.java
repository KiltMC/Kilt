package xyz.bluspring.kilt.helpers.mixin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

public class AnnotationValueVisitor extends AnnotationVisitor {
    public AnnotationValueVisitor() {
        super(Opcodes.ASM9);
    }

    public final Map<String, Object> values = new HashMap<>();

    @Override
    public void visit(String name, Object value) {
        super.visit(name, value);
        this.values.put(name, value);
    }
}
