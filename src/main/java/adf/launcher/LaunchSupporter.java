package adf.launcher;

import adf.launcher.annotation.NoStructureWarning;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class LaunchSupporter {

  private final String OPTION_MAKELAUNCHER = "-makelauncher";
  private final String OPTION_MAKESCRIPTS = "-makescripts";
  private final String OPTION_COMPILE = "-compile";
  private final String OPTION_JAVAHOME = "-javahome";
  private final String OPTION_CHECK = "-check";
  private final String OPTION_NOCHECK = "-nocheck";
  private final String OPTION_AUTOLOADERCLASS = "-autolc";
  private final String OPTION_UPDATECORE = "-updatecore";
  private final String OPTION_CHECKUPDATE = "-checkupdate";
  private final String DIRECTORY_LIBRARY = "library";
  private final String DIRECTORY_SRC = "src";
  private final String DIRECTORY_BUILD = "build";
  private final String CLASSNAME_LOADERPARENT = "adf.component.AbstractLoader";

  private int countAgentCheckWarning;
  private URL myCodeSource;
  private boolean isWindows;

  public LaunchSupporter() {
    countAgentCheckWarning = 0;
  }

  public void delegate(List<String> args) {
    boolean worked = false;
    String compilerJavaHome = null;
    isWindows = (System.getProperty("os.name").indexOf("Windows") == 0);

    alias(args, "-local", "-h", "localhost");
    alias(args, "-all", "-t", "-1,-1,-1,-1,-1,-1");
    alias(args, "-allp", "-t", "1,0,1,0,1,0");
    alias(args, "-precompute", "-pre", "true");
    alias(args, "-debug", "-d", "true");
    alias(args, "-develop", "-dev", "true");

    if (args.contains(OPTION_CHECKUPDATE)) {
      removeOption(args, OPTION_CHECKUPDATE);
      CoreUpdater coreUpdater = new CoreUpdater();
      if (coreUpdater.checkUpdate() && args.contains(OPTION_UPDATECORE)) {
        coreUpdater.updateCore();
        System.exit(5);
      }
      removeOption(args, OPTION_UPDATECORE);
    }

    if (args.contains(OPTION_UPDATECORE)) {
      removeOption(args, OPTION_UPDATECORE);
      (new CoreUpdater()).updateCore();
      System.exit(5);
    }

    if (args.contains(OPTION_JAVAHOME)) {
      removeOption(args, OPTION_JAVAHOME);
      int index = args.indexOf(OPTION_JAVAHOME) + 1;
      if (index < args.size()) {
        compilerJavaHome = args.get(index);
        args.remove(index);
      }
    }

    if (args.contains(OPTION_COMPILE)) {
      removeOption(args, OPTION_COMPILE);
      compileAgent(compilerJavaHome);
      args.add(OPTION_MAKELAUNCHER);
      worked = true;
    }

    if (args.contains(OPTION_MAKESCRIPTS)) {
      makeScript(compilerJavaHome, "compile", "-compile");
      makeScript(compilerJavaHome, "precompute", "adf.sample.SampleLoader", "-t", "$1,$2,$3,$4,$5,$6", "-h", "$7",
          "-pre", "true");
      makeScript(compilerJavaHome, "start", "adf.sample.SampleLoader", "-t", "$1,$2,$3,$4,$5,$6", "-h", "$7");
      args.add(OPTION_MAKELAUNCHER);
      worked = true;
    }

    if (args.contains(OPTION_MAKELAUNCHER)) {
      removeOption(args, OPTION_MAKELAUNCHER);
      makeLauncherScript(compilerJavaHome);
      worked = true;
    }

    if (args.contains(OPTION_CHECK) && (!args.contains(OPTION_NOCHECK))) {
      if (!args.contains(OPTION_MAKESCRIPTS)) {
        makeScript(compilerJavaHome, "check", "-check");
      }
      checkAgentClass(compilerJavaHome);
      worked = true;
    }
    removeOption(args, OPTION_MAKESCRIPTS);
    removeOption(args, OPTION_CHECK);
    removeOption(args, OPTION_NOCHECK);

    if (args.contains(OPTION_AUTOLOADERCLASS)) {
      removeOption(args, OPTION_AUTOLOADERCLASS);
      autoLoadDefaultLoaderClass(args);
    }

    if (args.size() <= 0) {
      if (!worked) {
        printOptionList();
      }
      System.exit(0);
    }
  }

  private void printOptionList() {
    System.out.println("* Please run following command when you update librarys.");
    System.out.println("java -jar library/rescue/adf/adf-core.jar -makelauncher");
    System.out.println("");
    System.out.println("");
    System.out.println("./launcher.sh {Options}");
    System.out.println("Options:");
    System.out.println("-makescripts\t\t\t\tmake launcher.sh");
    System.out.println("-makescripts\t\t\t\tmake utility scripts(start.sh, etc.)");
    System.out.println("-t [FB],[FS],[PF],[PO],[AT],[AC]\tnumber of agents");
    System.out.println("-fb [FB]\t\t\t\tnumber of FireBrigade");
    System.out.println("-fs [FS]\t\t\t\tnumber of FireStation");
    System.out.println("-pf [PF]\t\t\t\tnumber of PoliceForce");
    System.out.println("-po [PO]\t\t\t\tnumber of PoliceOffice");
    System.out.println("-at [AT]\t\t\t\tnumber of AmbulanceTeam");
    System.out.println("-ac [AC]\t\t\t\tnumber of AmbulanceCentre");
    System.out.println("-s [HOST]:[PORT]\t\t\tRCRS server host and port");
    System.out.println("-h [HOST]\t\t\t\tRCRS server host (port:27931)");
    System.out.println("-pre [0|1]\t\t\t\tPrecompute flag");
    System.out.println("-mc [FILE]\t\t\t\tModuleConfig file name");
    System.out.println("-md [JSON]\t\t\t\tModuleConfig JSON");
    System.out.println("-dev [0|1]\t\t\t\tDevelop flag");
    System.out.println("-dd [JSON]\t\t\t\tDevelopData JSON");
    System.out.println("-df [JSON File]\t\t\t\tDevelopData JSON file");
    System.out.println("-compile\t\t\t\trun compile (with -check)");
    System.out.println("-javahome [JAVA_HOME]\t\t\tcompiler java-home");
    // System.out.println("-autocp\t\t\t\t\tauto load class path form " +
    // DIRECTORY_LIBRARY);
    System.out.println("-autolc\t\t\t\t\tauto load loader class form " + DIRECTORY_BUILD);
    System.out.println("-d [0|1]\t\t\t\tDebug flag");
    System.out.println("-check\t\t\t\t\tsimple agent class check (with -autocp)");
    System.out.println("-checkupdate\t\t\t\tcheck adf-core update available");
    System.out.println("-updatecore\t\t\t\tupdate adf-core");
    // System.out.println("-auto\t\t\t\t\t[alias] -autocp -autolc");
    System.out.println("-all\t\t\t\t\t[alias] -t -1,-1,-1,-1,-1,-1");
    System.out.println("-allp\t\t\t\t\t[alias] -t 1,0,1,0,1,0");
    System.out.println("-local\t\t\t\t\t[alias] -h localhost");
    System.out.println("-precompute\t\t\t\t[alias] -pre true");
    System.out.println("-debug\t\t\t\t\t[alias] -d true");
    System.out.println("-develop\t\t\t\t[alias] -dev true");
    System.out.println();
  }

  private void alias(List<String> args, String option, String... original) {
    if (args.contains(option)) {
      removeOption(args, option);
      for (String org : original) {
        args.add(org);
      }
    }
  }

  private void removeOption(List<String> args, String option) {
    while (args.contains(option)) {
      args.remove(option);
    }
  }

  private void autoLoadDefaultLoaderClass(List<String> args) {
    args.add(0, getLoaderClass(DIRECTORY_BUILD));
  }

  private void autoLoadDefaultClassPath() {
    addClassPath(DIRECTORY_BUILD);

    String[] classPathArray = getClassPath(DIRECTORY_LIBRARY).split(System.getProperty("path.separator"), 0);
    for (String classPath : classPathArray) {
      if (classPath.length() > 0) {
        addClassPath(classPath);
      }
    }
  }

  private void addClassPath(String path) {
    URLClassLoader systemLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<?> systemClass = URLClassLoader.class;
    try {
      Method method = systemClass.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      method.invoke(systemLoader, new File(path).toURI().toURL());
    } catch (NoSuchMethodException | IllegalAccessException | MalformedURLException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private void makeLauncherScript(String javaHome) {
    String workDir = System.getProperty("user.dir");
    ConsoleOutput.info("launcher." + (isWindows ? "bat" : "sh") + " is made on " + workDir);
    String java = "java";
    if (javaHome != null) {
      ConsoleOutput.info("JAVA_HOME directory: " + javaHome);
      javaHome = Pattern.compile(File.separator + "$").matcher(javaHome).replaceFirst("");
      java = javaHome + File.separator + "bin" + File.separator + java;
    }
    String library = workDir + File.separator + DIRECTORY_LIBRARY;

    List<String> cmdArray = new ArrayList<>();
    cmdArray.add("cd `dirname $0`\n");
    cmdArray.add(java);
    cmdArray.add("-cp");
    cmdArray.add(getClassPath(library) + "build");
    cmdArray.add("adf.Main");
    if (isWindows) {
      cmdArray.add("%0 %1 %2 %3 %4 %5 %6 %7 %8 %9");
    } else {
      cmdArray.add("$*");
    }

    try {
      PrintWriter printWriter = new PrintWriter(
          new BufferedWriter(new FileWriter("launcher." + (isWindows ? "bat" : "sh"))));

      if (!isWindows) {
        printWriter.println("#!/bin/sh");
        printWriter.println();
      }
      for (String text : cmdArray) {
        printWriter.print(text);
        printWriter.append(' ');
      }
      printWriter.close();
      if (!isWindows) {
        try {
          ProcessBuilder processBuilder = new ProcessBuilder("chmod", "a+x", "launcher.sh");
          Process process = processBuilder.start();
          process.waitFor();
        } catch (IOException | InterruptedException e) {
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void makeScript(String javaHome, String name, String... commands) {
    String workDir = System.getProperty("user.dir");
    ConsoleOutput.info(name + "." + (isWindows ? "bat" : "sh") + " is made on " + workDir);

    List<String> cmdArray = new ArrayList<>();
    cmdArray.add("cd `dirname $0`\n");
    cmdArray.add("./launcher." + (isWindows ? "bat" : "sh"));
    cmdArray.addAll(Arrays.asList(commands));

    try {
      PrintWriter printWriter = new PrintWriter(
          new BufferedWriter(new FileWriter(name + "." + (isWindows ? "bat" : "sh"))));

      if (!isWindows) {
        printWriter.println("#!/bin/sh");
        printWriter.println();
      }
      for (String text : cmdArray) {
        printWriter.print(text);
        printWriter.append(' ');
      }
      printWriter.close();
      if (!isWindows) {
        try {
          ProcessBuilder processBuilder = new ProcessBuilder("chmod", "a+x", name + ".sh");
          Process process = processBuilder.start();
          process.waitFor();
        } catch (IOException | InterruptedException e) {
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void compileAgent(String javaHome) {
    ConsoleOutput.start("Agent compile");
    ConsoleOutput.info("MUST BE INVOKED VIA launcher.sh");
    String workDir = System.getProperty("user.dir");
    ConsoleOutput.info("Working directory: " + workDir);
    String javac = "javac";
    if (javaHome != null) {
      ConsoleOutput.info("JAVA_HOME directory: " + javaHome);
      javaHome = Pattern.compile(File.separator + "$").matcher(javaHome).replaceFirst("");
      javac = javaHome + File.separator + "bin" + File.separator + javac;
    }
    String library = workDir + File.separator + DIRECTORY_LIBRARY;
    String src = workDir + File.separator + DIRECTORY_SRC;
    String build = workDir + File.separator + DIRECTORY_BUILD;

    try {
      ProcessBuilder processBuilder = new ProcessBuilder(javac, "-version");
      Process process = processBuilder.start();
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      ConsoleOutput.error("Compiler(javac) is not found");
      System.exit(-1);
    }

    File libraryDir = new File(library);
    File srcDir = new File(src);
    if (!(libraryDir.isDirectory() && srcDir.isDirectory())) {
      ConsoleOutput.error("Does not have the required directory");
      System.exit(-1);
    }

    File buildDir = new File(build);
    deleteFile(buildDir);
    if (!(buildDir.mkdir())) {
      ConsoleOutput.error("Make build directory failed");
      System.exit(-1);
    }

    List<String> cmdArray = new ArrayList<>();
    cmdArray.add(javac);
    cmdArray.add("-cp");
    cmdArray.add(getClassPath(library));
    cmdArray.add("-d");
    cmdArray.add(".." + File.separator + DIRECTORY_BUILD + File.separator);
    cmdArray.addAll(getJavaFilesText(src));
    ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
    processBuilder.directory(srcDir);
    processBuilder.redirectErrorStream(true);

    try {
      Process process = processBuilder.start();
      InputStream is = process.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();

      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append(System.getProperty("line.separator"));
      }
      System.out.print(sb.toString());
      br.close();

      if (process.waitFor() != 0) {
        ConsoleOutput.error("Compile failed");
        System.exit(process.exitValue());
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

    ConsoleOutput.out(ConsoleOutput.State.FINISH, "Agent compile");
  }

  private void checkAgentClass(String javaHome) {
    countAgentCheckWarning = 0;
    myCodeSource = this.getClass().getProtectionDomain().getCodeSource().getLocation();

    if (!((new File(DIRECTORY_BUILD)).isDirectory())) {
      ConsoleOutput.error("Build directory is not found");
      System.exit(-1);
    }

    String jdeps = "jdeps";
    if (javaHome != null) {
      ConsoleOutput.info("JAVA_HOME directory: " + javaHome);
      javaHome = Pattern.compile(File.separator + "$").matcher(javaHome).replaceFirst("");
      jdeps = javaHome + File.separator + "bin" + File.separator + jdeps;
    }

    try {
      ProcessBuilder processBuilder = new ProcessBuilder(jdeps, "-version");
      Process process = processBuilder.start();
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      ConsoleOutput.error("Analyzer(jdeps) is not found");
      System.exit(-1);
    }

    ConsoleOutput.start("Agent class check");
    ConsoleOutput.info("MUST BE INVOKED VIA launcher.sh");
    checkAgentClass(DIRECTORY_BUILD, DIRECTORY_BUILD, jdeps);
    ConsoleOutput.finish(
        "Agent class check (" + countAgentCheckWarning + " warning" + (countAgentCheckWarning > 1 ? 's' : "") + ")");
  }

  @SuppressWarnings("unchecked")
  private void checkAgentClass(String base, String path, String jdeps) {
    File dir = new File(path);
    File[] files = dir.listFiles();
    if (files != null) {
      Arrays.sort(files, (a, b) -> (int) (b.lastModified() - a.lastModified()));
      for (File file : files) {
        if (file.isFile()) {
          String filePath = file.getPath();
          if (filePath.endsWith(".class") && !filePath.contains("$")) {
            boolean isAdfChild = false;
            boolean noStructureWarning = false;
            String loaderClass = filePath.substring(base.length() + 1, filePath.length() - 6).replace(File.separator,
                ".");

            try {
              Class clazz = ClassLoader.getSystemClassLoader().loadClass(loaderClass);

              ArrayList<String> methodList = new ArrayList<>();
              for (Method method : clazz.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                  String sign = method.getReturnType().getName() + ":" + method.getName() + ":";
                  for (Class paramClass : method.getParameterTypes()) {
                    sign += paramClass.getName() + ",";
                  }
                  methodList.add(sign);
                }
              }

              noStructureWarning = clazz.isAnnotationPresent(NoStructureWarning.class);

              clazz = clazz.getSuperclass();
              while (!(clazz.equals(java.lang.Object.class))) {
                for (Method method : clazz.getDeclaredMethods()) {
                  if (!(Modifier.isPrivate(method.getModifiers()))) {
                    String sign = method.getReturnType().getName() + ":" + method.getName() + ":";
                    for (Class paramClass : method.getParameterTypes()) {
                      sign += paramClass.getName() + ",";
                    }
                    methodList.remove(sign);

                    boolean hasGenericsParam = false;
                    for (int i = 0; (i < method.getParameterTypes().length) && (!hasGenericsParam); i++) {
                      Class<?> paramType = method.getParameterTypes()[i];
                      Type genericParamType = method.getGenericParameterTypes()[i];
                      if (!(paramType.getName().equals(genericParamType.getTypeName()))
                          && genericParamType.getTypeName().split("\\.").length == 1) {
                        hasGenericsParam = true;
                      }
                    }

                    if (hasGenericsParam) {
                      String regex = "^" + method.getReturnType().getName() + ":" + method.getName() + ":";
                      for (Class paramClass : method.getParameterTypes()) {
                        regex += ".*,";
                      }
                      regex += "$";

                      ArrayList<String> removeMethodList = new ArrayList<>();
                      for (String methodSign : methodList) {
                        if (methodSign.matches(regex)) {
                          removeMethodList.add(methodSign);
                        }
                      }
                      methodList.removeAll(removeMethodList);
                    }
                  }
                }

                if (clazz.getProtectionDomain().getCodeSource() != null
                    && clazz.getProtectionDomain().getCodeSource().getLocation().equals(myCodeSource)) {
                  isAdfChild = true;
                }

                clazz = clazz.getSuperclass();
              }

              if (isAdfChild && methodList.size() > 0) {
                ConsoleOutput.warn("Independent public method is exist in " + loaderClass + " :");
                countAgentCheckWarning++;
                for (String methodSign : methodList) {
                  String methodData[] = methodSign.split(":", 3);
                  String returnType[] = methodData[0].split("\\.");
                  String paramType[] = methodData[2].split(",");
                  System.out.print("\t" + returnType[returnType.length - 1] + " " + methodData[1] + "(");
                  boolean isFirst = true;
                  for (String name : paramType) {
                    if (isFirst) {
                      isFirst = false;
                    } else {
                      System.out.print(", ");
                    }
                    String splitedName[] = name.split("\\.");
                    System.out.print(splitedName[splitedName.length - 1]);
                  }
                  System.out.println(")");
                }
              }

              for (Field field : ClassLoader.getSystemClassLoader().loadClass(loaderClass).getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && !(Modifier.isFinal(field.getModifiers()))
                    && !(noStructureWarning)) {
                  ConsoleOutput.warn("Variable static field is exist : " + loaderClass + "." + field.getName());
                  countAgentCheckWarning++;
                } else if (isAdfChild && Modifier.isPublic(field.getModifiers())) {
                  ConsoleOutput.warn("Public field is exist : " + loaderClass + "." + field.getName());
                  countAgentCheckWarning++;
                }
              }

              if (!(noStructureWarning)) {
                List<String> cmdArray = new ArrayList<>();
                cmdArray.add(jdeps);
                cmdArray.add("-verbose:class");
                cmdArray.add("-e");
                cmdArray.add(loaderClass);
                cmdArray.add(DIRECTORY_BUILD);
                ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
                try {
                  Process process = processBuilder.start();
                  InputStream is = process.getInputStream();
                  BufferedReader br = new BufferedReader(new InputStreamReader(is));
                  ArrayList<String> dependClasses = new ArrayList<>();
                  String myPackage = loaderClass.substring(0, loaderClass.lastIndexOf('.'));

                  String line;
                  while ((line = br.readLine()) != null) {
                    if (line.charAt(line.length() - 1) == ')') {
                      line = line.trim();
                      line = line.substring(0, line.lastIndexOf(' '));
                      if (line.indexOf('.') > 0) {
                        dependClasses.add(line);
                      }
                    }
                  }
                  br.close();

                  if (process.waitFor() != 0) {
                    ConsoleOutput.error("Analyze failed");
                    System.exit(process.exitValue());
                  }

                  StringBuilder sb = new StringBuilder();
                  for (String className : dependClasses) {
                    if (myPackage.indexOf(className.substring(0, className.lastIndexOf('.'))) < 0) {
                      sb.append("\t\t").append(className).append(System.getProperty("line.separator"));
                    }
                  }

                  if (sb.length() > 0) {
                    countAgentCheckWarning++;
                    ConsoleOutput.warn("Violation structure (invoke the parent package class) :");
                    System.out.println("\t" + loaderClass + " ->");
                    System.out.print(sb);
                  }
                } catch (IOException | InterruptedException e) {
                  e.printStackTrace();
                }
              }
            } catch (ClassNotFoundException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }

    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          checkAgentClass(base, file.getPath(), jdeps);
        }
      }
    }
  }

  private String getLoaderClass(String base) {
    return getLoaderClass(base, base);
  }

  private String getLoaderClass(String base, String path) {
    String loaderClass;
    File dir = new File(path);
    File[] files = dir.listFiles();
    if (files != null) {
      Arrays.sort(files, (a, b) -> (int) (b.lastModified() - a.lastModified()));
      for (File file : files) {
        if (file.isFile()) {
          String filePath = file.getPath();
          if (filePath.endsWith(".class") && !filePath.contains("$")) {
            loaderClass = filePath.substring(base.length() + 1, filePath.length() - 6).replace(File.separator, ".");
            try {
              if (ClassLoader.getSystemClassLoader().loadClass(loaderClass).getSuperclass().getName()
                  .equals(CLASSNAME_LOADERPARENT)) {
                return loaderClass;
              }
            } catch (ClassNotFoundException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    loaderClass = "";

    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          loaderClass = getLoaderClass(base, file.getPath());
          if (!loaderClass.equals("")) {
            return loaderClass;
          }
        }
      }
    }

    return loaderClass;
  }

  private String getClassPath(String path) {
    String classPath = "";
    File dir = new File(path);
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          String filePath = file.getPath();
          if (filePath.endsWith(".jar") && !filePath.endsWith("-sources.jar")) {
            classPath += filePath + System.getProperty("path.separator");
          }
        } else if (file.isDirectory()) {
          classPath += getClassPath(file.getPath());
        }
      }
    }

    return classPath;
  }

  private List<String> getJavaFilesText(String path) {
    List<String> javaFilesText = new ArrayList<>();
    File dir = new File(path);
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          String filePath = file.getPath();
          if (filePath.endsWith(".java")) {
            javaFilesText.add(filePath);
          }
        } else if (file.isDirectory()) {
          javaFilesText.addAll(getJavaFilesText(file.getPath()));
        }
      }
    }

    return javaFilesText;
  }

  private void deleteFile(File file) {
    if (!file.exists()) {
      return;
    }

    if (file.isFile()) {
      if (!(file.delete())) {
        ConsoleOutput.error("Delete file failed");
        System.exit(-1);
      }
    } else if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File file1 : files) {
          deleteFile(file1);
        }
      }
      if (!(file.delete())) {
        ConsoleOutput.error("Delete file failed");
        System.exit(-1);
      }
    }
  }
}
