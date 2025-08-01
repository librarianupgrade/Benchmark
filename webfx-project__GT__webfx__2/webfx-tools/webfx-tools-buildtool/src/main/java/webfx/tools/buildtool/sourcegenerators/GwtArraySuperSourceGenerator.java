package webfx.tools.buildtool.sourcegenerators;

import webfx.tools.buildtool.ProjectModule;
import webfx.tools.buildtool.util.textfile.TextFileReaderWriter;

/**
 * @author Bruno Salmon
 */
final class GwtArraySuperSourceGenerator {

	private final static String TEMPLATE = "// Generated by WebFx\n" + "package java.lang.reflect;\n" + "\n"
			+ "import webfx.platform.shared.services.log.Logger;\n" + "\n" + "public final class Array {\n" + "\n"
			+ "    public static Object newInstance(Class<?> componentType, int length) throws NegativeArraySizeException {\n"
			+ "        switch (componentType.getName()) {\n" + "${generatedCasesCode}"
			+ "            // TYPE NOT FOUND\n" + "            default:\n"
			+ "               Logger.log(\"GWT super source Array.newInstance() has no case for type \" + componentType + \", so new Object[] is returned but this may cause a ClassCastException.\");\n"
			+ "               return new Object[length];\n" + "        }\n" + "    }\n" + "\n" + "}";

	static void generateArraySuperSource(ProjectModule module) {
		//GwtFilesGenerator.logSection("Generating " + module.getName() + " module java.lang.reflect.Array.java super source for GWT");
		StringBuilder sb = new StringBuilder();
		ProjectModule.filterProjectModules(module.getTransitiveModules())
				.flatMap(m -> m.getWebfxModuleFile().getArrayNewInstanceClasses()).distinct().stream().sorted()
				.forEach(className -> sb.append("            case \"").append(className).append("\": return new ")
						.append(className).append("[length];\n"));
		TextFileReaderWriter.writeTextFileIfNewOrModified(TEMPLATE.replace("${generatedCasesCode}", sb),
				module.getResourcesDirectory().resolve("super/java/lang/reflect/Array.java"));
	}
}
