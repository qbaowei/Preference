package com.qbw.annotation.preference.compiler;

import com.qbw.annotation.preference.Constant;
import com.qbw.annotation.preference.compiler.common.ClassNames;
import com.qbw.annotation.preference.compiler.common.CommonPoet;
import com.qbw.annotation.preference.compiler.common.VariableEnity;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * @author QBW
 * @createtime 2016/08/13 18:20
 */


public class ParasitePoet extends CommonPoet {

    protected final String M_REMOVE = "remove";

    private List<VariableEnity> mVariableNames;

    public ParasitePoet(Filer filer, String hostPackageName, String hostComplexClassName) {
        super(filer,
              hostPackageName,
              hostComplexClassName,
              hostPackageName,
              Constant.appendSuffix(hostComplexClassName));
        mVariableNames = new ArrayList<>();
    }

    public List<VariableEnity> getVariableNames() {
        return mVariableNames;
    }

    @Override
    protected List<FieldSpec> getFields() {
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        FieldSpec.Builder builder = FieldSpec.builder(ClassNames.PREFERENCE_UTIL, F_PUTIL);
        builder.addModifiers(Modifier.PRIVATE);
        fieldSpecs.add(builder.build());

        return fieldSpecs;
    }

    @Override
    protected List<MethodSpec> getMethods() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        MethodSpec.Builder builder = MethodSpec.constructorBuilder();
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("$L = $L.$L()", F_PUTIL, ClassNames.PREFERENCE, M_G_PUTIL);
        methodSpecs.add(builder.build());

        //save
        builder = MethodSpec.methodBuilder(M_SAVE).addModifiers(Modifier.PUBLIC);
        builder.addParameter(ParameterSpec.builder(mHostClassName, P_TARGET).build());
        for (VariableEnity variableEnity : mVariableNames) {
            if (!variableEnity.isDynamic()) {
                builder.addStatement("$L($L)", getSaveMethodNameNew(variableEnity), P_TARGET);
            }
        }
        methodSpecs.add(builder.build());

        //save every field
        boolean isSpecial = false;
        for (VariableEnity variableEnity : mVariableNames) {
            isSpecial = specialStart(variableEnity);
            if (variableEnity.isDynamic() || isSpecial) {
                builder = MethodSpec.methodBuilder(getSaveMethodNameNew(variableEnity));
                builder.addModifiers(Modifier.PUBLIC);
                builder.addParameter(ParameterSpec.builder(mHostClassName, P_TARGET).build());
                if (variableEnity.isDynamic()) {
                    builder.addParameter(ParameterSpec.builder(ClassNames.STRING, P_APPEND_KEY)
                                                      .build());
                    builder.addStatement("$L.put($L.getClass().getName() + $S + $L, $S, $L.$L)",
                                         F_PUTIL,
                                         P_TARGET,
                                         getKey(variableEnity.getName()) + "_",
                                         P_APPEND_KEY,
                                         variableEnity.getSimpleClassName(),
                                         P_TARGET,
                                         variableEnity.getName());
                    methodSpecs.add(builder.build());
                } else {
                    builder.addStatement("$L.put($L.getClass().getName() + $S, $S, $L.$L)",
                                         F_PUTIL,
                                         P_TARGET,
                                         getKey(variableEnity.getName()),
                                         variableEnity.getSimpleClassName(),
                                         P_TARGET,
                                         variableEnity.getName());
                    methodSpecs.add(builder.build());
                }
            }
            if (!variableEnity.isDynamic()) {
                builder = MethodSpec.methodBuilder(getSaveMethodName(variableEnity));
                builder.addModifiers(Modifier.PUBLIC);
                if (isSpecial) {
                    builder.addAnnotation(Deprecated.class);
                }
                builder.addParameter(ParameterSpec.builder(mHostClassName, P_TARGET).build());
                if (isSpecial) {
                    builder.addStatement("$L($L)", getSaveMethodNameNew(variableEnity), P_TARGET);
                } else {
                    builder.addStatement("$L.put($L.getClass().getName() + $S, $S, $L.$L)",
                                         F_PUTIL,
                                         P_TARGET,
                                         getKey(variableEnity.getName()),
                                         variableEnity.getSimpleClassName(),
                                         P_TARGET,
                                         variableEnity.getName());
                }
                methodSpecs.add(builder.build());
            }
        }

        //restore
        builder = MethodSpec.methodBuilder(M_RESTORE).addModifiers(Modifier.PUBLIC);
        builder.addParameter(ParameterSpec.builder(mHostClassName, P_TARGET).build());
        for (VariableEnity variableEnity : mVariableNames) {
            if (!variableEnity.isDynamic()) {
                builder.addStatement("$L($L)", getRestoreMethodNameNew(variableEnity), P_TARGET);
            }
        }
        methodSpecs.add(builder.build());

        //restore every field
        for (VariableEnity variableEnity : mVariableNames) {
            isSpecial = specialStart(variableEnity);
            if (variableEnity.isDynamic() || isSpecial) {
                builder = MethodSpec.methodBuilder(getRestoreMethodNameNew(variableEnity));
                builder.addModifiers(Modifier.PUBLIC);
                builder.addParameter(ParameterSpec.builder(mHostClassName, P_TARGET).build());
                if (variableEnity.isDynamic()) {
                    builder.addParameter(ParameterSpec.builder(ClassNames.STRING, P_APPEND_KEY)
                                                      .build());
                    builder.addStatement(
                            "$L.$L = ($T) $L.get($L.getClass().getName() + $S + $L, $S)",
                            P_TARGET,
                            variableEnity.getName(),
                            variableEnity.getPoetTypeName(),
                            F_PUTIL,
                            P_TARGET,
                            getKey(variableEnity.getName()) + "_",
                            P_APPEND_KEY,
                            variableEnity.getSimpleClassName());
                    methodSpecs.add(builder.build());
                } else {
                    builder.addStatement("$L.$L = ($T) $L.get($L.getClass().getName() + $S, $S)",
                                         P_TARGET,
                                         variableEnity.getName(),
                                         variableEnity.getPoetTypeName(),
                                         F_PUTIL,
                                         P_TARGET,
                                         getKey(variableEnity.getName()),
                                         variableEnity.getSimpleClassName());
                    methodSpecs.add(builder.build());
                }
            }
            if (!variableEnity.isDynamic()) {
                builder = MethodSpec.methodBuilder(getRestoreMethodName(variableEnity));
                builder.addModifiers(Modifier.PUBLIC);
                if (isSpecial) {
                    builder.addAnnotation(Deprecated.class);
                }
                builder.addParameter(ParameterSpec.builder(mHostClassName, P_TARGET).build());
                if (isSpecial) {
                    builder.addStatement("$L($L)",
                                         getRestoreMethodNameNew(variableEnity),
                                         P_TARGET);
                } else {
                    builder.addStatement("$L.$L = ($T) $L.get($L.getClass().getName() + $S, $S)",
                                         P_TARGET,
                                         variableEnity.getName(),
                                         variableEnity.getPoetTypeName(),
                                         F_PUTIL,
                                         P_TARGET,
                                         getKey(variableEnity.getName()),
                                         variableEnity.getSimpleClassName());
                }
                methodSpecs.add(builder.build());
            }
        }

        //remove
        builder = MethodSpec.methodBuilder(this.M_REMOVE).addModifiers(Modifier.PUBLIC);
        builder.addParameter(ParameterSpec.builder(mHostClassName, P_TARGET).build());
        for (VariableEnity variableEnity : mVariableNames) {
            if (!variableEnity.isDynamic()) {
                builder.addStatement("$L($L)", getRemoveMethodNameNew(variableEnity), P_TARGET);
            }
        }
        methodSpecs.add(builder.build());

        //remove every field
        for (VariableEnity variableEnity : mVariableNames) {
            isSpecial = specialStart(variableEnity);
            if (variableEnity.isDynamic() || isSpecial) {
                builder = MethodSpec.methodBuilder(getRemoveMethodNameNew(variableEnity));
                builder.addModifiers(Modifier.PUBLIC);
                builder.addParameter(ParameterSpec.builder(mHostClassName, P_TARGET).build());
                if (variableEnity.isDynamic()) {
                    builder.addParameter(ParameterSpec.builder(ClassNames.STRING, P_APPEND_KEY)
                                                      .build());
                    builder.addStatement("$L.$L($L.getClass().getName() + $S + $L)",
                                         F_PUTIL,
                                         M_REMOVE,
                                         P_TARGET,
                                         getKey(variableEnity.getName()) + "_",
                                         P_APPEND_KEY);
                    methodSpecs.add(builder.build());
                } else {
                    builder.addStatement("$L.$L($L.getClass().getName() + $S)",
                                         F_PUTIL,
                                         M_REMOVE,
                                         P_TARGET,
                                         getKey(variableEnity.getName()));
                    methodSpecs.add(builder.build());
                }
            }
            if (!variableEnity.isDynamic()) {
                builder = MethodSpec.methodBuilder(getRemoveMethodName(variableEnity));
                builder.addModifiers(Modifier.PUBLIC);
                if (isSpecial) {
                    builder.addAnnotation(Deprecated.class);
                }
                builder.addParameter(ParameterSpec.builder(mHostClassName, P_TARGET).build());
                if (isSpecial) {
                    builder.addStatement("$L($L)", getRemoveMethodNameNew(variableEnity), P_TARGET);
                } else {
                    builder.addStatement("$L.$L($L.getClass().getName() + $S)",
                                         F_PUTIL,
                                         M_REMOVE,
                                         P_TARGET,
                                         getKey(variableEnity.getName()));
                }
                methodSpecs.add(builder.build());
            }
        }

        return methodSpecs;
    }

    private String getKey(String variableName) {
        return Constant.INNER_LINK + variableName;
    }

    @Deprecated
    private String getSaveMethodName(VariableEnity variableEnity) {
        return M_SAVE + Character.toUpperCase(variableEnity.getName()
                                                           .charAt(0)) + variableEnity.getName()
                                                                                      .substring(1);
    }

    @Deprecated
    private String getRestoreMethodName(VariableEnity variableEnity) {
        return M_RESTORE + Character.toUpperCase(variableEnity.getName()
                                                              .charAt(0)) + variableEnity.getName()
                                                                                         .substring(
                                                                                                 1);
    }

    @Deprecated
    private String getRemoveMethodName(VariableEnity variableEnity) {
        return M_REMOVE + Character.toUpperCase(variableEnity.getName()
                                                             .charAt(0)) + variableEnity.getName()
                                                                                        .substring(1);
    }

    private String getSaveMethodNameNew(VariableEnity variableEnity) {
        String name = variableEnity.getName();
        if ((name.charAt(0) == 'm' || name.charAt(0) == 's') && Character.isUpperCase(name.charAt(1))) {
            return M_SAVE + name.substring(1);
        }
        return M_SAVE + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String getRestoreMethodNameNew(VariableEnity variableEnity) {
        String name = variableEnity.getName();
        if ((name.charAt(0) == 'm' || name.charAt(0) == 's') && Character.isUpperCase(name.charAt(1))) {
            return M_RESTORE + name.substring(1);
        }
        return M_RESTORE + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String getRemoveMethodNameNew(VariableEnity variableEnity) {
        String name = variableEnity.getName();
        if ((name.charAt(0) == 'm' || name.charAt(0) == 's') && Character.isUpperCase(name.charAt(1))) {
            return M_REMOVE + name.substring(1);
        }
        return M_REMOVE + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private boolean specialStart(VariableEnity variableEnity) {
        String name = variableEnity.getName();
        return (name.charAt(0) == 'm' || name.charAt(0) == 's') && Character.isUpperCase(name.charAt(
                1));
    }
}
