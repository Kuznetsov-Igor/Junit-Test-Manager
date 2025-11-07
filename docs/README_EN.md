# JUnit Test Manager Plugin

This plugin for IntelliJ IDEA simplifies managing and running JUnit tests, allowing you to group them by profiles and regular expressions, as well as automate the search and movement of test classes.

## Contents
- [Requirements](#requirements)
- [Button Location](#button-location)
- [Settings Location](#settings-location)
- [Settings](#settings)
- [Profiles](#profiles)
- [Groups](#groups)
- [Settings for Generating Test Data Objects](#settings-for-generating-test-data-objects)
- [Class Name for Generating Test Data Objects](#class-name-for-generating-test-data-objects)
- [Method Name for Generating Test Data Objects](#method-name-for-generating-test-data-objects)
- [Annotation Management](#annotation-management)
- [Generative Method Management](#generative-method-management)
- [Features](#features)
- [Examples](#examples)
- [Creating Configurations for Selected Tests](#creating-configurations-for-selected-tests)
- [Creating Configurations for All Tests or Groups](#creating-configurations-for-all-tests-or-groups)
- [Moving Test Classes Between Packages](#moving-test-classes-between-packages)

## Requirements
- IntelliJ IDEA 2024.1 or higher
- Java 17 for IntelliJ IDEA runtime (versions 2024.1 and higher already require and support this version)

## Button Location
The plugin buttons are available in the context menu when right-clicking in the code editor or in the project panel. They are grouped under **JUnit Test Manager**.

![Button Layout](images/1.png)

## Settings Location
Plugin settings are available in **Settings** → **Tools** → **Test Manager Settings**.

![Settings Location](images/2.png)

## Settings
The following options are available in the settings:
- Group Management
- Profile Management
- Interface Language Selection
- Logging Flag

### Profiles
Profiles determine which groups are active when grouping test classes and also affect VM arguments and color highlighting.

![Profiles](images/3.png)

### Groups
Groups are responsible for grouping test classes when creating run configurations and for defining VM arguments for these configurations. Groups allow you to automatically assign categories to test classes based on file paths and apply specific run settings.

![Groups](images/4.png)

![Group Parameters](images/5.png)

A group has 4 parameters:
- **Name**: The name of the group displayed in the plugin interface and in configurations.
- **Regular Expression**: Determines which group a test belongs to based on its path in the project. For example:
- If you want to select all converters in the `Convert` group (known to be in packages with `convert` in their names), use the regular expression `.*convert.*`. Now any test with `convert` in its path will automatically be assigned to this group. You can write more complex regular expressions—it's up to your imagination.
- If the regex is not specified or doesn't match, the test is placed in the default `Default` group. Regex supports standard Java syntax (Pattern.compile).
- **VM Arguments**: The Java Virtual Machine arguments with which the test classes in this group will be run. For example:
- `-Dparallel.tests=true -Dthreads=4` — for parallel test execution.
- `-Xmx2g` — to increase the heap size if the test classes require a lot of memory.
- **Color**: The color for visually highlighting the group in tables (cells are colored this color). Default is gray. This helps quickly distinguish groups in dialogs.
- **Profiles**: Determines which profiles this group is suitable for; they can be combined for different projects with similar groups.

Groups can be added, edited, and deleted directly from the settings. By default, the `Default` group (without regex and VM arguments) is always present; you can override it by creating your own without regex and linking it to the `Default` profile.

## Settings for generating Test Data objects
![Settings for generating Test Data objects](images/13.png)

To invoke this functionality, navigate to the object itself, open the context menu at the class level, and select `Generate` > `Generate Test Data Object`.

![Settings for generating Test Data objects](images/14.png)

The following options are available in the settings:
- Name of the class for generating Test Data objects
- Name of the method for generating Test Data objects
- Manage annotations
- Manage generative methods

### Name of the class for generating Test Data objects
Defines the prefix for the generated class with Test Data objects. By default, it will look like `public class ClassDataTestGenerator`.

### Name of the method for generating Test Data objects
Defines the prefix for the generated method with Test Data objects. By default, it will look like `public static ClassObjectData generateClassDataTest()`

### Annotation Management
Allows you to add, edit, and remove annotations that will be used when generating Test Data objects. For example, you can add the `@NonNull` annotation above a method to clearly indicate that we are generating a non-null object. It also allows you to specify it above the class.
```java
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ClassObjectDataGenerator {
    @NonNull
    public static ClassObjectData generateClassObjectData() {
        return new ClassObjectData(
                randomAlphabetic(20),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10)
        );
    }
}
```

#### Fields for adding annotations
- Specify the annotation itself (e.g. `@NonNull`)
- Specify `import` for this annotation (e.g. `import org.springframework.lang.NonNull`)
- Specify where the annotation will be added: the class (`CLASS`) or the method (`METHOD`)

### Managing Generative Methods
Allows you to add and remove generative methods that will be used when generating Test Data objects. For example, you can add the `randomAlphabetic(10)` method, which will generate a random string of letters of a certain length.

#### Fields for adding generative methods
- Specify the object type the method will be used for (e.g., `java.lang.String`)
- Specify the method itself (e.g., `randomAlphabetic(10)`)
- Specify `import` for this method (e.g., `import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic`)

## Functionality
Test classes are detected automatically:
- Classes with the `@Test` annotation (or `@ParameterizedTest` or similar).
- Classes inheriting from `junit.framework.TestCase`.
- Classes with a name ending in `Test`.

### 1. Find All Tests in Project
Finds **all** test classes in the project (recursively across all modules and test source roots).

### 2. Find Tests in Changes
Finds test classes that have been changed or added. For correct file tracking, it is recommended to connect the project to Git so that IDEA can detect changes.

### 3. Find Tests in Directory
Finds test classes in the selected directory (recursively). Available only by right-clicking a directory in the project.

### 4. Relocate Tests
Analyzes all classes in src/main/java, finds related test classes in the test source roots, and checks for package matches:
- The test must be in the same package as the class (e.g., com.example.MyClass → com.example.MyClassTest).
- The relationship is determined by the name (ClassName + "Test") and the presence of an import of the class in the test.
- If the packages don't match, offers to move the test to the class package (while maintaining the test source root). Shows a preview dialog.

### 5. Relocate Changes Tests
Similar to `Relocate Tests`, but only analyzes **changed** classes in the main source roots.

**Note**:
- Search and copying are supported for all tables.
- For the `Find Tests in Changes`, `Find All Tests in Project`, and `Find Tests in Directory` features:

- Search by class name only;
- When copying, only class paths are copied from a new line. If no specific test classes are selected, the configuration will be created for all tests by group;
- If selected, only the selected ones and by group.
- For the `Relocate Tests` and `Relocate Changes Tests` features:

- Search by class name only;
- When copying, only the class name is copied from a new line;
- Double-clicking on the class name opens the file in IDEA.

## Examples
Simple settings are used for the examples.

![Example settings](images/6.png)

### Creating configurations for selected tests
- The `package1` profile is active in the settings; The group has the regex `.*package1.*`.
- As you can see, one of our groups and the default group were detected.

![Found tests](images/7.png)

- For example, the first two classes were selected to create configurations only for them. As a result, two configurations were created, and the arguments specified for our group were applied to one of them.

![Created configurations](images/8.png)

### Creating configurations for all tests or groups
- Settings.

![Settings](images/9.png)

- Found test classes.

![Found tests](images/10.png)

- Our configurations for each group.

![Configurations](images/11.png)

**Note**: A test configuration can have N test classes per group; they are specified using Pattern.

### Moving test classes across packages
The settings are the same as before; they do not affect this functionality. We analyze files in the main source root and find test classes for them.
- We have a class named `NameClass2` located in `package2`, but we decided to move it to another package, for example, `package3`.
- Our test for this class remains in `package2`; it has the correct name `NameClass2Test` and the imports reference `NameClass2` itself, so this test class is suitable for moving. We know where it should be defined.

![Move Dialog](images/12.png)

- After moving, the test class will be placed in `package3` only in the test source root.

**Note**: Only test classes with a selected checkbox are moved. You can select or clear all checkboxes by clicking the 'Selected' heading.

### In Case of Issues
- **Regex not working**: Make sure the regular expression matches Java Pattern syntax. Test it with file paths.
- **Tests not found**: Make sure the classes match the criteria (annotations, inheritance, or name). Also, make sure the test source roots are configured correctly in the project.
- **Moving broke imports**: This shouldn't happen, but if it does, manually update the imports in the test after moving if they weren't updated automatically.
- If other issues arise, enable logging in the plugin settings and check the logs for diagnostics.
- **Test Data objects not generated**: Make sure you're calling generation at the class level and that the class contains the fields to generate. Check the generation settings and ensure the required annotations and methods are specified correctly. There may also be an error due to project indexing, and the plugin simply doesn't recognize the package in which Test Data objects need to be generated. The quickest solution is to create this package and rerun the command.

### Contribute
- If you have ideas for improving the plugin or would like to report a bug, please create an [issue](https://github.com/Kuznetsov-Igor/Junit-Test-Manager/issues)
- Development support is welcome! You can always create a pull request with improvements or new features.

### Tested on
Tested:
- Ubuntu 20.04.6 LTS
- IntelliJ IDEA 2024.1.6 (Community Edition) Runtime version: 17.0.11+1-b1207.30 amd64, VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
- IntelliJ IDEA 2025.2.3 (Community Edition) Runtime version: 21.0.8+9-b1038.72 amd64 (JCEF 122.1.9) VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
- Windows 10 Pro 22H2 19045.6456
- IntelliJ IDEA 2024.1.4 (Ultimate Edition), Runtime version: 17.0.11+1-b1207.24 amd64, VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.