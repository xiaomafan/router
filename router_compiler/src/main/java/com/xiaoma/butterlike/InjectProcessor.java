package com.xiaoma.butterlike;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.xiaoma.annotation.inject.Inject;
import com.xiaoma.annotation.inject.InjectUriParam;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class InjectProcessor extends AbstractProcessor {

    private Filer filer;
    private Elements elementUtils;
    private TypeTool typeTools;
    private static final List<Class<? extends Annotation>> ANNOTATIONS = Arrays.asList(//
            Inject.class,
            InjectUriParam.class
    );

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        Types typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        typeTools = new TypeTool(typeUtils, elementUtils);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, TargetClass> targetClassMap = findAndParseTargets(roundEnv);
        for (Map.Entry<TypeElement,TargetClass> entry:targetClassMap.entrySet()){
            TypeElement typeElement = entry.getKey();
            TargetClass targetClass = entry.getValue();
            JavaFile javaFile = targetClass.brewJava();

            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                error(typeElement,"Unable to write injecting for type %s: %s"
                        ,typeElement,e.getMessage());
            }
        }
        return true;
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(kind, message, element);
    }

    private Map<TypeElement, TargetClass> findAndParseTargets(RoundEnvironment roundEnv) {
        Map<TypeElement, TargetClass> targetClassMap = new LinkedHashMap<>();
        for (Class<? extends Annotation> clazz : ANNOTATIONS) {
            for (Element element : roundEnv.getElementsAnnotatedWith(clazz)) {
                parseInjectParam(element, targetClassMap, clazz);
            }
        }
        return targetClassMap;
    }

    private void parseInjectParam(Element element, Map<TypeElement, TargetClass> targetClassMap,
                                  Class<? extends Annotation> clazz) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        TargetClass targetClass = goOrCreateTargetClass(targetClassMap, element, enclosingElement);

        String paramKey = "";
        if (clazz.equals(Inject.class)) {
            paramKey = element.getAnnotation(Inject.class).value();
        } else if (clazz.equals(InjectUriParam.class)) {
            paramKey = element.getAnnotation(InjectUriParam.class).value();
        }

        String name = element.getSimpleName().toString();
        if (paramKey.length() == 0) {
            paramKey = name;
        }

        FiledInjecting filedInjecting = new FiledInjecting(name, element.asType(), paramKey, clazz);
        targetClass.addField(filedInjecting);

    }

    private TargetClass goOrCreateTargetClass(Map<TypeElement, TargetClass> targetClassMap,
                                              Element element, TypeElement enclosingElement) {
        TargetClass targetClass = targetClassMap.get(enclosingElement);
        if (targetClass == null) {
            TypeName targetType = TypeName.get(enclosingElement.asType());
            if (targetType instanceof ParameterizedTypeName) {
                targetType = ((ParameterizedTypeName) targetType).rawType;
            }
            String packageName = getPackageName(enclosingElement);
            String className = getClassName(enclosingElement, packageName);
            ClassName bindingClassName = ClassName.get(packageName, className + "_RouterInjecting");
            targetClass = new TargetClass(typeTools, targetType, bindingClassName);
            targetClassMap.put(enclosingElement, targetClass);
        }
        return targetClass;
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private List<Class<? extends Annotation>> getSupportedAnnotations() {
        return ANNOTATIONS;
    }


}
