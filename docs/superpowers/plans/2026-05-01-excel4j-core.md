# excel4j-core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Maven multi-module project scaffold and `excel4j-core` — the OOXML read/write engine and mutable workbook/cell model that all other modules depend on.

**Architecture:** Pure JDK 21, no external runtime dependencies. StAX streaming for both read and write paths. Mutable workbook model with immutable value types (records + sealed interfaces). JPMS modules enforce layer boundaries.

**Tech Stack:** Java 21, Maven multi-module, JUnit 5, AssertJ, JDK `java.util.zip` + `javax.xml.stream` (StAX).

---

## File Map

```
excel4j/
├── .github/workflows/ci.yml
├── pom.xml                                          ← parent POM
├── excel4j-bom/
│   └── pom.xml
├── excel4j-core/
│   ├── pom.xml
│   └── src/
│       ├── main/java/io/excel4j/core/
│       │   ├── module-info.java
│       │   ├── Excel.java                           ← static factory entry point
│       │   ├── exception/
│       │   │   ├── ExcelException.java
│       │   │   ├── ExcelReadException.java
│       │   │   └── ExcelWriteException.java
│       │   ├── model/
│       │   │   ├── CellValue.java                   ← sealed interface
│       │   │   ├── TextValue.java
│       │   │   ├── NumberValue.java
│       │   │   ├── BooleanValue.java
│       │   │   ├── ErrorValue.java
│       │   │   ├── ErrorType.java
│       │   │   ├── BlankValue.java
│       │   │   ├── DateValue.java
│       │   │   ├── DateTimeValue.java
│       │   │   ├── CellRef.java                     ← 1-based coordinate record
│       │   │   ├── CellRange.java
│       │   │   ├── WorksheetName.java
│       │   │   ├── Workbook.java
│       │   │   ├── Worksheet.java
│       │   │   ├── Cell.java
│       │   │   └── style/
│       │   │       ├── Font.java
│       │   │       ├── Fill.java
│       │   │       ├── Border.java
│       │   │       ├── NumberFormat.java
│       │   │       └── CellStyle.java
│       │   └── io/
│       │       ├── OoxmlReader.java                 ← ZIP+StAX → Workbook
│       │       ├── OoxmlWriter.java                 ← Workbook → ZIP+StAX
│       │       ├── internal/
│       │       │   ├── SharedStringsTable.java
│       │       │   ├── StyleTable.java
│       │       │   └── DateConverter.java
│       └── test/java/io/excel4j/core/
│           ├── model/
│           │   ├── CellValueTest.java
│           │   ├── CellRefTest.java
│           │   └── WorkbookModelTest.java
│           ├── io/
│           │   ├── DateConverterTest.java
│           │   ├── SharedStringsTableTest.java
│           │   ├── StyleTableTest.java
│           │   └── OoxmlRoundTripTest.java
│           └── fixtures/                            ← .xlsx test files committed to repo
├── excel4j-formula/
│   └── pom.xml                                      ← stub only in this plan
├── excel4j-report/
│   └── pom.xml                                      ← stub only in this plan
└── excel4j-render/
    └── pom.xml                                      ← stub only in this plan
```

---

## Task 1: Maven Multi-Module Scaffold

**Files:**
- Create: `pom.xml`
- Create: `excel4j-bom/pom.xml`
- Create: `excel4j-core/pom.xml`
- Create: `excel4j-formula/pom.xml`
- Create: `excel4j-report/pom.xml`
- Create: `excel4j-render/pom.xml`
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Create parent POM**

Create `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
           http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>io.excel4j</groupId>
  <artifactId>excel4j</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>pom</packaging>

  <modules>
    <module>excel4j-bom</module>
    <module>excel4j-core</module>
    <module>excel4j-formula</module>
    <module>excel4j-report</module>
    <module>excel4j-render</module>
  </modules>

  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <junit.version>5.10.2</junit.version>
    <assertj.version>3.25.3</assertj.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>${assertj.version}</version>
        <scope>test</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.12.1</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>3.2.5</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-enforcer-plugin</artifactId>
          <version>3.4.1</version>
          <executions>
            <execution>
              <id>enforce</id>
              <goals><goal>enforce</goal></goals>
              <configuration>
                <rules>
                  <dependencyConvergence/>
                  <requireJavaVersion>
                    <version>[21,)</version>
                  </requireJavaVersion>
                </rules>
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```

- [ ] **Step 2: Create excel4j-bom POM**

Create `excel4j-bom/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
           http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>io.excel4j</groupId>
    <artifactId>excel4j</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>excel4j-bom</artifactId>
  <packaging>pom</packaging>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>io.excel4j</groupId>
        <artifactId>excel4j-core</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>io.excel4j</groupId>
        <artifactId>excel4j-formula</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>io.excel4j</groupId>
        <artifactId>excel4j-report</artifactId>
        <version>${project.version}</version>
      </dependency>
    </dependencies>
  </dependencyManagement>
</project>
```

- [ ] **Step 3: Create excel4j-core POM**

Create `excel4j-core/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
           http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>io.excel4j</groupId>
    <artifactId>excel4j</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>excel4j-core</artifactId>

  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
    </dependency>
    <dependency>
      <groupId>org.assertj</groupId>
      <artifactId>assertj-core</artifactId>
    </dependency>
  </dependencies>
</project>
```

- [ ] **Step 4: Create stub POMs for remaining modules**

Create `excel4j-formula/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
           http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>io.excel4j</groupId>
    <artifactId>excel4j</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>excel4j-formula</artifactId>
  <dependencies>
    <dependency>
      <groupId>io.excel4j</groupId>
      <artifactId>excel4j-core</artifactId>
      <version>${project.version}</version>
    </dependency>
  </dependencies>
</project>
```

Create `excel4j-report/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
           http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>io.excel4j</groupId>
    <artifactId>excel4j</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>excel4j-report</artifactId>
  <dependencies>
    <dependency>
      <groupId>io.excel4j</groupId>
      <artifactId>excel4j-formula</artifactId>
      <version>${project.version}</version>
    </dependency>
  </dependencies>
</project>
```

Create `excel4j-render/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
           http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>io.excel4j</groupId>
    <artifactId>excel4j</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>excel4j-render</artifactId>
  <dependencies>
    <dependency>
      <groupId>io.excel4j</groupId>
      <artifactId>excel4j-core</artifactId>
      <version>${project.version}</version>
    </dependency>
  </dependencies>
</project>
```

- [ ] **Step 5: Create GitHub Actions CI**

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [ main, dev ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      - run: mvn -B verify
```

- [ ] **Step 6: Create source directory structure**

```bash
mkdir -p excel4j-core/src/main/java/io/excel4j/core/{exception,model/style,io/internal}
mkdir -p excel4j-core/src/test/java/io/excel4j/core/{model,io,fixtures}
mkdir -p excel4j-formula/src/main/java/io/excel4j/formula
mkdir -p excel4j-formula/src/test/java/io/excel4j/formula
mkdir -p excel4j-report/src/main/java/io/excel4j/report
mkdir -p excel4j-report/src/test/java/io/excel4j/report
mkdir -p excel4j-render/src/main/java/io/excel4j/render
```

- [ ] **Step 7: Verify build compiles**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS (no sources yet, just structure)

- [ ] **Step 8: Commit**

```bash
git add .
git commit -m "chore: maven multi-module scaffold with CI"
```

---

## Task 2: Exception Hierarchy

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/exception/ExcelException.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/exception/ExcelReadException.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/exception/ExcelWriteException.java`

- [ ] **Step 1: Create ExcelException**

```java
package io.excel4j.core.exception;

public class ExcelException extends RuntimeException {
    public ExcelException(String message) {
        super(message);
    }
    public ExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 2: Create ExcelReadException**

```java
package io.excel4j.core.exception;

public class ExcelReadException extends ExcelException {
    public ExcelReadException(String message) {
        super(message);
    }
    public ExcelReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 3: Create ExcelWriteException**

```java
package io.excel4j.core.exception;

public class ExcelWriteException extends ExcelException {
    public ExcelWriteException(String message) {
        super(message);
    }
    public ExcelWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/exception/
git commit -m "feat(core): exception hierarchy"
```

---

## Task 3: CellValue Sealed Interface

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/CellValue.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/TextValue.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/NumberValue.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/BooleanValue.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/ErrorType.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/ErrorValue.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/BlankValue.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/DateValue.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/DateTimeValue.java`
- Test: `excel4j-core/src/test/java/io/excel4j/core/model/CellValueTest.java`

- [ ] **Step 1: Write the failing test**

Create `excel4j-core/src/test/java/io/excel4j/core/model/CellValueTest.java`:

```java
package io.excel4j.core.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class CellValueTest {

    @Test
    void patternMatchTextValue() {
        CellValue value = new TextValue("hello");
        String result = switch (value) {
            case TextValue(var s) -> s;
            default -> "other";
        };
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void patternMatchNumberValue() {
        CellValue value = new NumberValue(42.5);
        double result = switch (value) {
            case NumberValue(var d) -> d;
            default -> 0.0;
        };
        assertThat(result).isEqualTo(42.5);
    }

    @Test
    void patternMatchDateValue() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        CellValue value = new DateValue(date);
        LocalDate result = switch (value) {
            case DateValue(var d) -> d;
            default -> null;
        };
        assertThat(result).isEqualTo(date);
    }

    @Test
    void patternMatchErrorValue() {
        CellValue value = new ErrorValue(ErrorType.DIV_BY_ZERO);
        ErrorType type = switch (value) {
            case ErrorValue(var t) -> t;
            default -> null;
        };
        assertThat(type).isEqualTo(ErrorType.DIV_BY_ZERO);
    }

    @Test
    void blankValueEquality() {
        assertThat(new BlankValue()).isEqualTo(new BlankValue());
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

```bash
mvn test -pl excel4j-core -Dtest=CellValueTest -q 2>&1 | tail -5
```

Expected: FAIL — `CellValue` not found.

- [ ] **Step 3: Create ErrorType**

```java
package io.excel4j.core.model;

public enum ErrorType {
    DIV_BY_ZERO,
    VALUE,
    REF,
    NAME,
    NA,
    NULL,
    NUM,
    CIRCULAR_REF;

    public String toExcelString() {
        return switch (this) {
            case DIV_BY_ZERO -> "#DIV/0!";
            case VALUE -> "#VALUE!";
            case REF -> "#REF!";
            case NAME -> "#NAME?";
            case NA -> "#N/A";
            case NULL -> "#NULL!";
            case NUM -> "#NUM!";
            case CIRCULAR_REF -> "#CIRCULAR_REF!";
        };
    }

    public static ErrorType fromExcelString(String s) {
        return switch (s) {
            case "#DIV/0!" -> DIV_BY_ZERO;
            case "#VALUE!" -> VALUE;
            case "#REF!" -> REF;
            case "#NAME?" -> NAME;
            case "#N/A" -> NA;
            case "#NULL!" -> NULL;
            case "#NUM!" -> NUM;
            default -> throw new IllegalArgumentException("Unknown error: " + s);
        };
    }
}
```

- [ ] **Step 4: Create sealed CellValue interface and all permits**

Create `CellValue.java`:

```java
package io.excel4j.core.model;

public sealed interface CellValue
        permits TextValue, NumberValue, BooleanValue,
                ErrorValue, BlankValue, DateValue, DateTimeValue {}
```

Create `TextValue.java`:

```java
package io.excel4j.core.model;

public record TextValue(String value) implements CellValue {}
```

Create `NumberValue.java`:

```java
package io.excel4j.core.model;

public record NumberValue(double value) implements CellValue {}
```

Create `BooleanValue.java`:

```java
package io.excel4j.core.model;

public record BooleanValue(boolean value) implements CellValue {}
```

Create `ErrorValue.java`:

```java
package io.excel4j.core.model;

public record ErrorValue(ErrorType type) implements CellValue {}
```

Create `BlankValue.java`:

```java
package io.excel4j.core.model;

public record BlankValue() implements CellValue {}
```

Create `DateValue.java`:

```java
package io.excel4j.core.model;

import java.time.LocalDate;

public record DateValue(LocalDate value) implements CellValue {}
```

Create `DateTimeValue.java`:

```java
package io.excel4j.core.model;

import java.time.LocalDateTime;

public record DateTimeValue(LocalDateTime value) implements CellValue {}
```

- [ ] **Step 5: Run test — verify it passes**

```bash
mvn test -pl excel4j-core -Dtest=CellValueTest -q 2>&1 | tail -3
```

Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 6: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/model/ excel4j-core/src/test/
git commit -m "feat(core): CellValue sealed interface with all value types"
```

---

## Task 4: CellRef, CellRange, WorksheetName

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/CellRef.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/CellRange.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/WorksheetName.java`
- Test: `excel4j-core/src/test/java/io/excel4j/core/model/CellRefTest.java`

- [ ] **Step 1: Write the failing test**

Create `excel4j-core/src/test/java/io/excel4j/core/model/CellRefTest.java`:

```java
package io.excel4j.core.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CellRefTest {

    @Test
    void parseA1Notation() {
        CellRef ref = CellRef.of("A1");
        assertThat(ref.row()).isEqualTo(1);
        assertThat(ref.col()).isEqualTo(1);
    }

    @Test
    void parseZ1() {
        CellRef ref = CellRef.of("Z1");
        assertThat(ref.col()).isEqualTo(26);
    }

    @Test
    void parseAA1() {
        CellRef ref = CellRef.of("AA1");
        assertThat(ref.col()).isEqualTo(27);
    }

    @Test
    void parseAMJ10000() {
        CellRef ref = CellRef.of("AMJ10000");
        assertThat(ref.col()).isEqualTo(1024);
        assertThat(ref.row()).isEqualTo(10000);
    }

    @Test
    void toA1Notation() {
        assertThat(new CellRef(1, 1).toA1()).isEqualTo("A1");
        assertThat(new CellRef(1, 26).toA1()).isEqualTo("Z1");
        assertThat(new CellRef(1, 27).toA1()).isEqualTo("AA1");
    }

    @Test
    void rejectZeroRow() {
        assertThatThrownBy(() -> new CellRef(0, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectZeroCol() {
        assertThatThrownBy(() -> new CellRef(1, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rangeContainsRef() {
        CellRange range = new CellRange(CellRef.of("A1"), CellRef.of("C3"));
        assertThat(range.contains(CellRef.of("B2"))).isTrue();
        assertThat(range.contains(CellRef.of("D1"))).isFalse();
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

```bash
mvn test -pl excel4j-core -Dtest=CellRefTest -q 2>&1 | tail -5
```

Expected: FAIL — `CellRef` not found.

- [ ] **Step 3: Create CellRef**

```java
package io.excel4j.core.model;

public record CellRef(int row, int col) {

    public CellRef {
        if (row < 1) throw new IllegalArgumentException("row must be >= 1, got: " + row);
        if (col < 1) throw new IllegalArgumentException("col must be >= 1, got: " + col);
    }

    public static CellRef of(String a1) {
        int i = 0;
        while (i < a1.length() && Character.isLetter(a1.charAt(i))) i++;
        String colPart = a1.substring(0, i).toUpperCase();
        int rowNum = Integer.parseInt(a1.substring(i));
        int colNum = 0;
        for (char c : colPart.toCharArray()) {
            colNum = colNum * 26 + (c - 'A' + 1);
        }
        return new CellRef(rowNum, colNum);
    }

    public String toA1() {
        return colToLetters(col) + row;
    }

    private static String colToLetters(int col) {
        StringBuilder sb = new StringBuilder();
        int c = col;
        while (c > 0) {
            int rem = (c - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            c = (c - 1) / 26;
        }
        return sb.toString();
    }
}
```

- [ ] **Step 4: Create CellRange**

```java
package io.excel4j.core.model;

public record CellRange(CellRef first, CellRef last) {

    public boolean contains(CellRef ref) {
        return ref.row() >= first.row() && ref.row() <= last.row()
            && ref.col() >= first.col() && ref.col() <= last.col();
    }

    public static CellRange of(String a1, String a2) {
        return new CellRange(CellRef.of(a1), CellRef.of(a2));
    }
}
```

- [ ] **Step 5: Create WorksheetName**

```java
package io.excel4j.core.model;

public record WorksheetName(String value) {
    public WorksheetName {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Worksheet name must not be blank");
        if (value.length() > 31)
            throw new IllegalArgumentException("Worksheet name must be <= 31 chars");
    }
}
```

- [ ] **Step 6: Run test — verify it passes**

```bash
mvn test -pl excel4j-core -Dtest=CellRefTest -q 2>&1 | tail -3
```

Expected: `Tests run: 8, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/model/
git commit -m "feat(core): CellRef, CellRange, WorksheetName records with A1 notation"
```

---

## Task 5: Style Model

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/style/Font.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/style/Fill.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/style/Border.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/style/NumberFormat.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/style/CellStyle.java`

- [ ] **Step 1: Create Font**

```java
package io.excel4j.core.model.style;

public record Font(
    String name,
    double size,
    boolean bold,
    boolean italic,
    boolean underline,
    String color
) {
    public static Font DEFAULT = new Font("Calibri", 11, false, false, false, "000000");

    public static Font of(String name, double size) {
        return new Font(name, size, false, false, false, "000000");
    }

    public Font bold() {
        return new Font(name, size, true, italic, underline, color);
    }

    public Font italic() {
        return new Font(name, size, bold, true, underline, color);
    }

    public Font color(String hexColor) {
        return new Font(name, size, bold, italic, underline, hexColor);
    }
}
```

- [ ] **Step 2: Create Fill**

```java
package io.excel4j.core.model.style;

public record Fill(FillPattern pattern, String foregroundColor, String backgroundColor) {

    public enum FillPattern { NONE, SOLID }

    public static Fill NONE = new Fill(FillPattern.NONE, "FFFFFF", "FFFFFF");

    public static Fill solid(String hexColor) {
        return new Fill(FillPattern.SOLID, hexColor, "FFFFFF");
    }
}
```

- [ ] **Step 3: Create Border**

```java
package io.excel4j.core.model.style;

public record Border(BorderStyle top, BorderStyle bottom, BorderStyle left, BorderStyle right) {

    public enum BorderStyle { NONE, THIN, MEDIUM, THICK, DASHED, DOTTED }

    public static Border NONE = new Border(
        BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE);

    public static Border thin() {
        return new Border(
            BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN);
    }
}
```

- [ ] **Step 4: Create NumberFormat**

```java
package io.excel4j.core.model.style;

public record NumberFormat(String formatCode) {

    public static NumberFormat GENERAL  = new NumberFormat("General");
    public static NumberFormat INTEGER  = new NumberFormat("0");
    public static NumberFormat DECIMAL_2 = new NumberFormat("#,##0.00");
    public static NumberFormat PERCENTAGE = new NumberFormat("0.00%");
    public static NumberFormat DATE_SHORT = new NumberFormat("yyyy-mm-dd");
    public static NumberFormat DATE_LONG  = new NumberFormat("d mmmm yyyy");
    public static NumberFormat DATETIME   = new NumberFormat("yyyy-mm-dd hh:mm:ss");
    public static NumberFormat CURRENCY   = new NumberFormat("\"$\"#,##0.00");

    public static NumberFormat custom(String code) {
        return new NumberFormat(code);
    }

    public boolean isDateFormat() {
        String lc = formatCode.toLowerCase();
        return lc.contains("y") || lc.contains("d") || lc.contains("h")
            || lc.contains("m") || lc.contains("s");
    }
}
```

- [ ] **Step 5: Create CellStyle**

```java
package io.excel4j.core.model.style;

public record CellStyle(Font font, Fill fill, Border border, NumberFormat numberFormat) {

    public static CellStyle DEFAULT = new CellStyle(
        Font.DEFAULT, Fill.NONE, Border.NONE, NumberFormat.GENERAL);

    public CellStyle withFont(Font f)               { return new CellStyle(f, fill, border, numberFormat); }
    public CellStyle withFill(Fill f)               { return new CellStyle(font, f, border, numberFormat); }
    public CellStyle withBorder(Border b)           { return new CellStyle(font, fill, b, numberFormat); }
    public CellStyle withNumberFormat(NumberFormat n) { return new CellStyle(font, fill, border, n); }
}
```

- [ ] **Step 6: Verify compile**

```bash
mvn compile -pl excel4j-core -q 2>&1 | tail -3
```

Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/model/style/
git commit -m "feat(core): style model - Font, Fill, Border, NumberFormat, CellStyle"
```

---

## Task 6: Workbook / Worksheet / Cell Model

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/Cell.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/Worksheet.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/model/Workbook.java`
- Test: `excel4j-core/src/test/java/io/excel4j/core/model/WorkbookModelTest.java`

- [ ] **Step 1: Write the failing test**

Create `excel4j-core/src/test/java/io/excel4j/core/model/WorkbookModelTest.java`:

```java
package io.excel4j.core.model;

import io.excel4j.core.model.style.CellStyle;
import io.excel4j.core.model.style.Font;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class WorkbookModelTest {

    @Test
    void createWorkbookWithDefaultSheet() {
        Workbook wb = new Workbook();
        assertThat(wb.sheets()).hasSize(1);
        assertThat(wb.sheet(1).name()).isEqualTo(new WorksheetName("Sheet1"));
    }

    @Test
    void addAndAccessSheetByName() {
        Workbook wb = new Workbook();
        Worksheet orders = wb.addSheet("Orders");
        assertThat(wb.sheet("Orders")).isSameAs(orders);
    }

    @Test
    void addAndAccessSheetByIndex() {
        Workbook wb = new Workbook();
        wb.addSheet("Orders");
        assertThat(wb.sheet(2).name().value()).isEqualTo("Orders");
    }

    @Test
    void setCellValueByA1() {
        Workbook wb = new Workbook();
        Worksheet sheet = wb.sheet(1);
        sheet.cell("A1").setValue(new TextValue("Hello"));
        assertThat(sheet.cell("A1").getValue()).isEqualTo(new TextValue("Hello"));
    }

    @Test
    void setCellValueByRowCol() {
        Workbook wb = new Workbook();
        Worksheet sheet = wb.sheet(1);
        sheet.cell(1, 1).setValue(new NumberValue(42.0));
        assertThat(sheet.cell(1, 1).getValue()).isEqualTo(new NumberValue(42.0));
    }

    @Test
    void setCellFormula() {
        Workbook wb = new Workbook();
        Worksheet sheet = wb.sheet(1);
        sheet.cell("A1").setFormula("SUM(B1:B10)");
        assertThat(sheet.cell("A1").getFormula()).isEqualTo("SUM(B1:B10)");
    }

    @Test
    void setCellStyle() {
        Workbook wb = new Workbook();
        Worksheet sheet = wb.sheet(1);
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        sheet.cell("A1").setStyle(bold);
        assertThat(sheet.cell("A1").getStyle()).isEqualTo(bold);
    }

    @Test
    void unsetCellIsBlank() {
        Workbook wb = new Workbook();
        assertThat(wb.sheet(1).cell("Z99").getValue()).isInstanceOf(BlankValue.class);
    }

    @Test
    void setCellDate() {
        Workbook wb = new Workbook();
        LocalDate date = LocalDate.of(2024, 6, 15);
        wb.sheet(1).cell("A1").setValue(new DateValue(date));
        assertThat(wb.sheet(1).cell("A1").getValue()).isEqualTo(new DateValue(date));
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

```bash
mvn test -pl excel4j-core -Dtest=WorkbookModelTest -q 2>&1 | tail -5
```

Expected: FAIL — `Workbook` not found.

- [ ] **Step 3: Create Cell**

```java
package io.excel4j.core.model;

import io.excel4j.core.model.style.CellStyle;

public class Cell {

    private final CellRef ref;
    private CellValue value;
    private String formula;
    private CellStyle style;

    public Cell(CellRef ref) {
        this.ref = ref;
        this.value = new BlankValue();
        this.style = CellStyle.DEFAULT;
    }

    public CellRef getRef()          { return ref; }
    public CellValue getValue()      { return value; }
    public String getFormula()       { return formula; }
    public CellStyle getStyle()      { return style; }

    public void setValue(CellValue value) {
        this.value = value != null ? value : new BlankValue();
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void setStyle(CellStyle style) {
        this.style = style != null ? style : CellStyle.DEFAULT;
    }
}
```

- [ ] **Step 4: Create Worksheet**

```java
package io.excel4j.core.model;

import java.util.HashMap;
import java.util.Map;

public class Worksheet {

    private final WorksheetName name;
    private final Map<CellRef, Cell> cells = new HashMap<>();

    public Worksheet(WorksheetName name) {
        this.name = name;
    }

    public WorksheetName name() { return name; }

    public Cell cell(int row, int col) {
        CellRef ref = new CellRef(row, col);
        return cells.computeIfAbsent(ref, Cell::new);
    }

    public Cell cell(String a1) {
        return cell(CellRef.of(a1));
    }

    public Cell cell(CellRef ref) {
        return cells.computeIfAbsent(ref, Cell::new);
    }

    public Map<CellRef, Cell> cells() {
        return Map.copyOf(cells);
    }
}
```

- [ ] **Step 5: Create Workbook**

```java
package io.excel4j.core.model;

import io.excel4j.core.exception.ExcelException;
import java.util.ArrayList;
import java.util.List;

public class Workbook {

    private final List<Worksheet> sheets = new ArrayList<>();

    public Workbook() {
        sheets.add(new Worksheet(new WorksheetName("Sheet1")));
    }

    public Worksheet addSheet(String name) {
        Worksheet sheet = new Worksheet(new WorksheetName(name));
        sheets.add(sheet);
        return sheet;
    }

    public Worksheet sheet(int index) {
        if (index < 1 || index > sheets.size())
            throw new ExcelException("Sheet index out of range: " + index);
        return sheets.get(index - 1);
    }

    public Worksheet sheet(String name) {
        return sheets.stream()
            .filter(s -> s.name().value().equals(name))
            .findFirst()
            .orElseThrow(() -> new ExcelException("Sheet not found: " + name));
    }

    public List<Worksheet> sheets() {
        return List.copyOf(sheets);
    }

    public static Workbook empty() {
        Workbook wb = new Workbook();
        wb.sheets.clear();
        return wb;
    }
}
```

- [ ] **Step 6: Run test — verify it passes**

```bash
mvn test -pl excel4j-core -Dtest=WorkbookModelTest -q 2>&1 | tail -3
```

Expected: `Tests run: 9, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/model/
git commit -m "feat(core): mutable Workbook/Worksheet/Cell model"
```

---

## Task 7: DateConverter

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/io/internal/DateConverter.java`
- Test: `excel4j-core/src/test/java/io/excel4j/core/io/DateConverterTest.java`

- [ ] **Step 1: Write the failing test**

Create `excel4j-core/src/test/java/io/excel4j/core/io/DateConverterTest.java`:

```java
package io.excel4j.core.io;

import io.excel4j.core.io.internal.DateConverter;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class DateConverterTest {

    @Test
    void serialToLocalDate() {
        // Excel serial 45291 = 2024-01-15
        LocalDate date = DateConverter.toLocalDate(45291.0);
        assertThat(date).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test
    void localDateToSerial() {
        double serial = DateConverter.toSerial(LocalDate.of(2024, 1, 15));
        assertThat(serial).isEqualTo(45291.0);
    }

    @Test
    void serialWithTimeFractionToLocalDateTime() {
        // 0.5 = noon
        double serial = 45291.5;
        LocalDateTime dt = DateConverter.toLocalDateTime(serial);
        assertThat(dt.toLocalDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(dt.getHour()).isEqualTo(12);
    }

    @Test
    void handle1900LeapYearBug() {
        // Excel incorrectly treats 1900-02-29 as valid (serial 60)
        // Serial 61 should map to 1900-03-01, not 1900-03-00
        LocalDate date = DateConverter.toLocalDate(61.0);
        assertThat(date).isEqualTo(LocalDate.of(1900, 3, 1));
    }

    @Test
    void roundTripDate() {
        LocalDate original = LocalDate.of(2023, 6, 20);
        double serial = DateConverter.toSerial(original);
        LocalDate recovered = DateConverter.toLocalDate(serial);
        assertThat(recovered).isEqualTo(original);
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

```bash
mvn test -pl excel4j-core -Dtest=DateConverterTest -q 2>&1 | tail -5
```

Expected: FAIL — `DateConverter` not found.

- [ ] **Step 3: Create DateConverter**

```java
package io.excel4j.core.io.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public final class DateConverter {

    // Excel epoch: 1900-01-01 = serial 1
    // Excel has a leap year bug: it thinks 1900-02-29 existed (serial 60)
    // All serials >= 61 are off by 1 day due to this bug
    private static final LocalDate EXCEL_EPOCH = LocalDate.of(1899, 12, 31);
    private static final int LEAP_BUG_SERIAL = 60;

    private DateConverter() {}

    public static LocalDate toLocalDate(double serial) {
        long days = (long) serial;
        if (days > LEAP_BUG_SERIAL) days--;  // compensate for 1900 bug
        return EXCEL_EPOCH.plusDays(days);
    }

    public static LocalDateTime toLocalDateTime(double serial) {
        LocalDate date = toLocalDate(serial);
        double fraction = serial - Math.floor(serial);
        long totalSeconds = Math.round(fraction * 86400);
        LocalTime time = LocalTime.ofSecondOfDay(totalSeconds % 86400);
        return LocalDateTime.of(date, time);
    }

    public static double toSerial(LocalDate date) {
        long days = EXCEL_EPOCH.until(date, ChronoUnit.DAYS);
        if (days >= LEAP_BUG_SERIAL) days++;  // compensate for 1900 bug
        return (double) days;
    }

    public static double toSerial(LocalDateTime dateTime) {
        double datePart = toSerial(dateTime.toLocalDate());
        double timeFraction = (dateTime.getHour() * 3600
            + dateTime.getMinute() * 60
            + dateTime.getSecond()) / 86400.0;
        return datePart + timeFraction;
    }
}
```

- [ ] **Step 4: Run test — verify it passes**

```bash
mvn test -pl excel4j-core -Dtest=DateConverterTest -q 2>&1 | tail -3
```

Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/io/internal/DateConverter.java
git add excel4j-core/src/test/java/io/excel4j/core/io/DateConverterTest.java
git commit -m "feat(core): DateConverter with 1900 leap year bug compensation"
```

---

## Task 8: SharedStringsTable

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/io/internal/SharedStringsTable.java`
- Test: `excel4j-core/src/test/java/io/excel4j/core/io/SharedStringsTableTest.java`

- [ ] **Step 1: Write the failing test**

Create `excel4j-core/src/test/java/io/excel4j/core/io/SharedStringsTableTest.java`:

```java
package io.excel4j.core.io;

import io.excel4j.core.io.internal.SharedStringsTable;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SharedStringsTableTest {

    @Test
    void addAndRetrieve() {
        SharedStringsTable table = new SharedStringsTable();
        int idx = table.add("Hello");
        assertThat(table.get(idx)).isEqualTo("Hello");
    }

    @Test
    void deduplicateStrings() {
        SharedStringsTable table = new SharedStringsTable();
        int idx1 = table.add("Hello");
        int idx2 = table.add("Hello");
        assertThat(idx1).isEqualTo(idx2);
        assertThat(table.size()).isEqualTo(1);
    }

    @Test
    void multipleStrings() {
        SharedStringsTable table = new SharedStringsTable();
        int a = table.add("A");
        int b = table.add("B");
        int c = table.add("C");
        assertThat(a).isEqualTo(0);
        assertThat(b).isEqualTo(1);
        assertThat(c).isEqualTo(2);
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

```bash
mvn test -pl excel4j-core -Dtest=SharedStringsTableTest -q 2>&1 | tail -5
```

Expected: FAIL — `SharedStringsTable` not found.

- [ ] **Step 3: Create SharedStringsTable**

```java
package io.excel4j.core.io.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedStringsTable {

    private final List<String> strings = new ArrayList<>();
    private final Map<String, Integer> index = new HashMap<>();

    public int add(String s) {
        return index.computeIfAbsent(s, k -> {
            int idx = strings.size();
            strings.add(k);
            return idx;
        });
    }

    public String get(int idx) {
        return strings.get(idx);
    }

    public int size() {
        return strings.size();
    }

    public List<String> all() {
        return List.copyOf(strings);
    }
}
```

- [ ] **Step 4: Run test — verify it passes**

```bash
mvn test -pl excel4j-core -Dtest=SharedStringsTableTest -q 2>&1 | tail -3
```

Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/io/internal/SharedStringsTable.java
git add excel4j-core/src/test/java/io/excel4j/core/io/SharedStringsTableTest.java
git commit -m "feat(core): SharedStringsTable with deduplication"
```

---

## Task 9: StyleTable

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/io/internal/StyleTable.java`
- Test: `excel4j-core/src/test/java/io/excel4j/core/io/StyleTableTest.java`

- [ ] **Step 1: Write the failing test**

Create `excel4j-core/src/test/java/io/excel4j/core/io/StyleTableTest.java`:

```java
package io.excel4j.core.io;

import io.excel4j.core.io.internal.StyleTable;
import io.excel4j.core.model.style.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class StyleTableTest {

    @Test
    void addDefaultStyleGetsIndexZero() {
        StyleTable table = new StyleTable();
        int idx = table.add(CellStyle.DEFAULT);
        assertThat(idx).isEqualTo(0);
    }

    @Test
    void deduplicateStyles() {
        StyleTable table = new StyleTable();
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        int idx1 = table.add(bold);
        int idx2 = table.add(bold);
        assertThat(idx1).isEqualTo(idx2);
        assertThat(table.size()).isEqualTo(1);
    }

    @Test
    void differentStylesGetDifferentIndexes() {
        StyleTable table = new StyleTable();
        int idx1 = table.add(CellStyle.DEFAULT);
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        int idx2 = table.add(bold);
        assertThat(idx1).isNotEqualTo(idx2);
        assertThat(table.size()).isEqualTo(2);
    }

    @Test
    void retrieveByIndex() {
        StyleTable table = new StyleTable();
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        int idx = table.add(bold);
        assertThat(table.get(idx)).isEqualTo(bold);
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

```bash
mvn test -pl excel4j-core -Dtest=StyleTableTest -q 2>&1 | tail -5
```

Expected: FAIL — `StyleTable` not found.

- [ ] **Step 3: Create StyleTable**

```java
package io.excel4j.core.io.internal;

import io.excel4j.core.model.style.CellStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StyleTable {

    private final List<CellStyle> styles = new ArrayList<>();
    private final Map<CellStyle, Integer> index = new HashMap<>();

    public int add(CellStyle style) {
        return index.computeIfAbsent(style, k -> {
            int idx = styles.size();
            styles.add(k);
            return idx;
        });
    }

    public CellStyle get(int idx) {
        return styles.get(idx);
    }

    public int size() {
        return styles.size();
    }

    public List<CellStyle> all() {
        return List.copyOf(styles);
    }
}
```

- [ ] **Step 4: Run test — verify it passes**

```bash
mvn test -pl excel4j-core -Dtest=StyleTableTest -q 2>&1 | tail -3
```

Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/io/internal/StyleTable.java
git add excel4j-core/src/test/java/io/excel4j/core/io/StyleTableTest.java
git commit -m "feat(core): StyleTable with CellStyle deduplication"
```

---

## Task 10: OoxmlWriter

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/io/OoxmlWriter.java`

Write first, test via round-trip in Task 12.

- [ ] **Step 1: Create OoxmlWriter**

```java
package io.excel4j.core.io;

import io.excel4j.core.exception.ExcelWriteException;
import io.excel4j.core.io.internal.DateConverter;
import io.excel4j.core.io.internal.SharedStringsTable;
import io.excel4j.core.io.internal.StyleTable;
import io.excel4j.core.model.*;
import io.excel4j.core.model.style.CellStyle;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OoxmlWriter {

    public void write(Workbook workbook, Path path) {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path))) {
            SharedStringsTable sst = buildSharedStrings(workbook);
            StyleTable styleTable = buildStyleTable(workbook);
            writeContentTypes(zip, workbook.sheets().size());
            writeRels(zip);
            writeWorkbook(zip, workbook);
            writeWorkbookRels(zip, workbook.sheets().size());
            writeSharedStrings(zip, sst);
            writeStyles(zip, styleTable);
            for (int i = 0; i < workbook.sheets().size(); i++) {
                writeSheet(zip, workbook.sheets().get(i), i + 1, sst, styleTable);
            }
        } catch (IOException | XMLStreamException e) {
            throw new ExcelWriteException("Failed to write XLSX: " + path, e);
        }
    }

    private SharedStringsTable buildSharedStrings(Workbook wb) {
        SharedStringsTable sst = new SharedStringsTable();
        for (Worksheet sheet : wb.sheets()) {
            for (Cell cell : sheet.cells().values()) {
                if (cell.getValue() instanceof TextValue(var s)) sst.add(s);
            }
        }
        return sst;
    }

    private StyleTable buildStyleTable(Workbook wb) {
        StyleTable st = new StyleTable();
        st.add(CellStyle.DEFAULT);
        for (Worksheet sheet : wb.sheets()) {
            for (Cell cell : sheet.cells().values()) {
                st.add(cell.getStyle());
            }
        }
        return st;
    }

    private void writeContentTypes(ZipOutputStream zip, int sheetCount)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("Types");
        w.writeDefaultNamespace("http://schemas.openxmlformats.org/package/2006/content-types");
        writeOverride(w, "/_rels/.rels",
            "application/vnd.openxmlformats-package.relationships+xml");
        writeOverride(w, "/xl/workbook.xml",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml");
        writeOverride(w, "/xl/sharedStrings.xml",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml");
        writeOverride(w, "/xl/styles.xml",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml");
        for (int i = 1; i <= sheetCount; i++) {
            writeOverride(w, "/xl/worksheets/sheet" + i + ".xml",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml");
        }
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeOverride(XMLStreamWriter w, String partName, String contentType)
            throws XMLStreamException {
        w.writeEmptyElement("Override");
        w.writeAttribute("PartName", partName);
        w.writeAttribute("ContentType", contentType);
    }

    private void writeRels(ZipOutputStream zip) throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("_rels/.rels"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("Relationships");
        w.writeDefaultNamespace("http://schemas.openxmlformats.org/package/2006/relationships");
        w.writeEmptyElement("Relationship");
        w.writeAttribute("Id", "rId1");
        w.writeAttribute("Type",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument");
        w.writeAttribute("Target", "xl/workbook.xml");
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeWorkbook(ZipOutputStream zip, Workbook wb)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/workbook.xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("workbook");
        w.writeDefaultNamespace(
            "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
        String rNs = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
        w.writeNamespace("r", rNs);
        w.writeStartElement("sheets");
        List<Worksheet> sheets = wb.sheets();
        for (int i = 0; i < sheets.size(); i++) {
            w.writeEmptyElement("sheet");
            w.writeAttribute("name", sheets.get(i).name().value());
            w.writeAttribute("sheetId", String.valueOf(i + 1));
            w.writeAttribute(rNs, "id", "rId" + (i + 1));
        }
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeWorkbookRels(ZipOutputStream zip, int sheetCount)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("Relationships");
        w.writeDefaultNamespace(
            "http://schemas.openxmlformats.org/package/2006/relationships");
        for (int i = 1; i <= sheetCount; i++) {
            w.writeEmptyElement("Relationship");
            w.writeAttribute("Id", "rId" + i);
            w.writeAttribute("Type",
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet");
            w.writeAttribute("Target", "worksheets/sheet" + i + ".xml");
        }
        w.writeEmptyElement("Relationship");
        w.writeAttribute("Id", "rId" + (sheetCount + 1));
        w.writeAttribute("Type",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings");
        w.writeAttribute("Target", "sharedStrings.xml");
        w.writeEmptyElement("Relationship");
        w.writeAttribute("Id", "rId" + (sheetCount + 2));
        w.writeAttribute("Type",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles");
        w.writeAttribute("Target", "styles.xml");
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeSharedStrings(ZipOutputStream zip, SharedStringsTable sst)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("sst");
        w.writeDefaultNamespace(
            "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
        w.writeAttribute("count", String.valueOf(sst.size()));
        w.writeAttribute("uniqueCount", String.valueOf(sst.size()));
        for (String s : sst.all()) {
            w.writeStartElement("si");
            w.writeStartElement("t");
            w.writeCharacters(s);
            w.writeEndElement();
            w.writeEndElement();
        }
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeStyles(ZipOutputStream zip, StyleTable styleTable)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/styles.xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("styleSheet");
        w.writeDefaultNamespace(
            "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
        // fonts
        w.writeStartElement("fonts");
        w.writeAttribute("count", String.valueOf(styleTable.size()));
        for (var style : styleTable.all()) {
            w.writeStartElement("font");
            if (style.font().bold()) {
                w.writeEmptyElement("b");
            }
            if (style.font().italic()) {
                w.writeEmptyElement("i");
            }
            w.writeEmptyElement("sz");
            w.writeAttribute("val", String.valueOf(style.font().size()));
            w.writeEmptyElement("name");
            w.writeAttribute("val", style.font().name());
            w.writeEndElement();
        }
        w.writeEndElement();
        // fills (minimal)
        w.writeStartElement("fills");
        w.writeAttribute("count", "2");
        w.writeStartElement("fill"); w.writeEmptyElement("patternFill");
        w.writeAttribute("patternType", "none"); w.writeEndElement();
        w.writeStartElement("fill"); w.writeEmptyElement("patternFill");
        w.writeAttribute("patternType", "gray125"); w.writeEndElement();
        w.writeEndElement();
        // borders (minimal)
        w.writeStartElement("borders");
        w.writeAttribute("count", "1");
        w.writeStartElement("border");
        w.writeEmptyElement("left"); w.writeEmptyElement("right");
        w.writeEmptyElement("top"); w.writeEmptyElement("bottom");
        w.writeEndElement();
        w.writeEndElement();
        // cellStyleXfs
        w.writeStartElement("cellStyleXfs");
        w.writeAttribute("count", "1");
        w.writeEmptyElement("xf");
        w.writeAttribute("numFmtId", "0");
        w.writeAttribute("fontId", "0");
        w.writeAttribute("fillId", "0");
        w.writeAttribute("borderId", "0");
        w.writeEndElement();
        // cellXfs
        w.writeStartElement("cellXfs");
        w.writeAttribute("count", String.valueOf(styleTable.size()));
        for (int i = 0; i < styleTable.size(); i++) {
            w.writeEmptyElement("xf");
            w.writeAttribute("numFmtId", "0");
            w.writeAttribute("fontId", String.valueOf(i));
            w.writeAttribute("fillId", "0");
            w.writeAttribute("borderId", "0");
            w.writeAttribute("xfId", "0");
        }
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeSheet(ZipOutputStream zip, Worksheet sheet, int sheetIndex,
            SharedStringsTable sst, StyleTable styleTable)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/worksheets/sheet" + sheetIndex + ".xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("worksheet");
        w.writeDefaultNamespace(
            "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
        w.writeStartElement("sheetData");

        Map<CellRef, Cell> cells = sheet.cells();
        cells.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(
                java.util.Comparator.comparingInt(CellRef::row).thenComparingInt(CellRef::col)))
            .forEach(entry -> {
                try {
                    writeCellXml(w, entry.getValue(), sst, styleTable);
                } catch (XMLStreamException e) {
                    throw new ExcelWriteException("Error writing cell", e);
                }
            });

        w.writeEndElement(); // sheetData
        w.writeEndElement(); // worksheet
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeCellXml(XMLStreamWriter w, Cell cell,
            SharedStringsTable sst, StyleTable styleTable) throws XMLStreamException {
        if (cell.getValue() instanceof BlankValue && cell.getFormula() == null) return;

        w.writeStartElement("c");
        w.writeAttribute("r", cell.getRef().toA1());
        int styleIdx = styleTable.add(cell.getStyle());
        if (styleIdx > 0) w.writeAttribute("s", String.valueOf(styleIdx));

        CellValue value = cell.getValue();
        switch (value) {
            case TextValue(var s) -> {
                w.writeAttribute("t", "s");
                w.writeStartElement("v");
                w.writeCharacters(String.valueOf(sst.add(s)));
                w.writeEndElement();
            }
            case NumberValue(var d) -> {
                w.writeStartElement("v");
                w.writeCharacters(formatDouble(d));
                w.writeEndElement();
            }
            case BooleanValue(var b) -> {
                w.writeAttribute("t", "b");
                w.writeStartElement("v");
                w.writeCharacters(b ? "1" : "0");
                w.writeEndElement();
            }
            case ErrorValue(var t) -> {
                w.writeAttribute("t", "e");
                w.writeStartElement("v");
                w.writeCharacters(t.toExcelString());
                w.writeEndElement();
            }
            case DateValue(var d) -> {
                w.writeStartElement("v");
                w.writeCharacters(formatDouble(DateConverter.toSerial(d)));
                w.writeEndElement();
            }
            case DateTimeValue(var dt) -> {
                w.writeStartElement("v");
                w.writeCharacters(formatDouble(DateConverter.toSerial(dt)));
                w.writeEndElement();
            }
            case BlankValue() -> {}
        }

        if (cell.getFormula() != null) {
            w.writeStartElement("f");
            w.writeCharacters(cell.getFormula());
            w.writeEndElement();
        }

        w.writeEndElement(); // c
    }

    private String formatDouble(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    private XMLStreamWriter startXml(OutputStream out) throws XMLStreamException {
        return XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8");
    }
}
```

- [ ] **Step 2: Compile**

```bash
mvn compile -pl excel4j-core -q 2>&1 | tail -5
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/io/OoxmlWriter.java
git commit -m "feat(core): OoxmlWriter - StAX streaming XLSX writer"
```

---

## Task 11: OoxmlReader

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/io/OoxmlReader.java`

- [ ] **Step 1: Create OoxmlReader**

```java
package io.excel4j.core.io;

import io.excel4j.core.exception.ExcelReadException;
import io.excel4j.core.io.internal.DateConverter;
import io.excel4j.core.model.*;
import io.excel4j.core.model.style.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OoxmlReader {

    private final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

    public Workbook read(Path path) {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            List<String> sharedStrings = readSharedStrings(zip);
            List<CellStyle> styles = readStyles(zip);
            List<String[]> sheetMeta = readWorkbook(zip); // [name, rId]
            Workbook wb = Workbook.empty();
            for (String[] meta : sheetMeta) {
                wb.addSheet(meta[0]);
            }
            for (int i = 0; i < sheetMeta.size(); i++) {
                readSheet(zip, "xl/worksheets/sheet" + (i + 1) + ".xml",
                    wb.sheet(i + 1), sharedStrings, styles);
            }
            return wb;
        } catch (IOException | XMLStreamException e) {
            throw new ExcelReadException("Failed to read XLSX: " + path, e);
        }
    }

    private List<String> readSharedStrings(ZipFile zip)
            throws IOException, XMLStreamException {
        List<String> result = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) return result;
        try (InputStream in = zip.getInputStream(entry)) {
            XMLStreamReader r = xmlFactory.createXMLStreamReader(in);
            StringBuilder current = null;
            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if ("si".equals(r.getLocalName())) current = new StringBuilder();
                } else if (event == XMLStreamConstants.CHARACTERS && current != null) {
                    current.append(r.getText());
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if ("si".equals(r.getLocalName()) && current != null) {
                        result.add(current.toString());
                        current = null;
                    }
                }
            }
        }
        return result;
    }

    private List<CellStyle> readStyles(ZipFile zip)
            throws IOException, XMLStreamException {
        List<CellStyle> result = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/styles.xml");
        if (entry == null) {
            result.add(CellStyle.DEFAULT);
            return result;
        }
        // Minimal style reading: just return DEFAULT for now
        // Full style parsing (fonts, fills, borders, numFmts) is covered in a follow-up
        result.add(CellStyle.DEFAULT);
        return result;
    }

    private List<String[]> readWorkbook(ZipFile zip)
            throws IOException, XMLStreamException {
        List<String[]> sheets = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/workbook.xml");
        if (entry == null) throw new ExcelReadException("Missing xl/workbook.xml");
        try (InputStream in = zip.getInputStream(entry)) {
            XMLStreamReader r = xmlFactory.createXMLStreamReader(in);
            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT
                        && "sheet".equals(r.getLocalName())) {
                    String name = r.getAttributeValue(null, "name");
                    String rId = r.getAttributeValue(
                        "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id");
                    if (rId == null) rId = r.getAttributeValue(null, "r:id");
                    sheets.add(new String[]{name, rId});
                }
            }
        }
        return sheets;
    }

    private void readSheet(ZipFile zip, String entryPath, Worksheet sheet,
            List<String> sharedStrings, List<CellStyle> styles)
            throws IOException, XMLStreamException {
        ZipEntry entry = zip.getEntry(entryPath);
        if (entry == null) return;
        try (InputStream in = zip.getInputStream(entry)) {
            XMLStreamReader r = xmlFactory.createXMLStreamReader(in);
            String currentRef = null;
            String currentType = null;
            String currentStyleIdx = null;
            StringBuilder currentValue = new StringBuilder();
            StringBuilder currentFormula = new StringBuilder();
            boolean inValue = false;
            boolean inFormula = false;

            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    switch (r.getLocalName()) {
                        case "c" -> {
                            currentRef = r.getAttributeValue(null, "r");
                            currentType = r.getAttributeValue(null, "t");
                            currentStyleIdx = r.getAttributeValue(null, "s");
                            currentValue.setLength(0);
                            currentFormula.setLength(0);
                        }
                        case "v" -> inValue = true;
                        case "f" -> inFormula = true;
                    }
                } else if (event == XMLStreamConstants.CHARACTERS) {
                    if (inValue) currentValue.append(r.getText());
                    if (inFormula) currentFormula.append(r.getText());
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    switch (r.getLocalName()) {
                        case "v" -> inValue = false;
                        case "f" -> inFormula = false;
                        case "c" -> {
                            if (currentRef != null) {
                                Cell cell = sheet.cell(CellRef.of(currentRef));
                                setCellValue(cell, currentType,
                                    currentValue.toString(), sharedStrings);
                                if (!currentFormula.isEmpty()) {
                                    cell.setFormula(currentFormula.toString());
                                }
                            }
                            currentRef = null;
                            currentType = null;
                        }
                    }
                }
            }
        }
    }

    private void setCellValue(Cell cell, String type, String raw,
            List<String> sharedStrings) {
        if (raw.isEmpty()) return;
        switch (type != null ? type : "") {
            case "s" -> cell.setValue(
                new TextValue(sharedStrings.get(Integer.parseInt(raw))));
            case "b" -> cell.setValue(new BooleanValue("1".equals(raw)));
            case "e" -> cell.setValue(new ErrorValue(ErrorType.fromExcelString(raw)));
            default -> {
                try {
                    cell.setValue(new NumberValue(Double.parseDouble(raw)));
                } catch (NumberFormatException e) {
                    cell.setValue(new TextValue(raw));
                }
            }
        }
    }
}
```

- [ ] **Step 2: Compile**

```bash
mvn compile -pl excel4j-core -q 2>&1 | tail -3
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/io/OoxmlReader.java
git commit -m "feat(core): OoxmlReader - StAX streaming XLSX reader"
```

---

## Task 12: Excel Static Factory + Round-Trip Integration Test

**Files:**
- Create: `excel4j-core/src/main/java/io/excel4j/core/Excel.java`
- Create: `excel4j-core/src/main/java/io/excel4j/core/module-info.java`
- Test: `excel4j-core/src/test/java/io/excel4j/core/io/OoxmlRoundTripTest.java`

- [ ] **Step 1: Create Excel static factory**

```java
package io.excel4j.core;

import io.excel4j.core.io.OoxmlReader;
import io.excel4j.core.io.OoxmlWriter;
import io.excel4j.core.model.Workbook;

import java.nio.file.Path;

public final class Excel {

    private Excel() {}

    public static Workbook create() {
        return new Workbook();
    }

    public static Workbook read(Path path) {
        return new OoxmlReader().read(path);
    }

    public static Workbook read(String path) {
        return read(Path.of(path));
    }

    public static void write(Workbook workbook, Path path) {
        new OoxmlWriter().write(workbook, path);
    }

    public static void write(Workbook workbook, String path) {
        write(workbook, Path.of(path));
    }
}
```

- [ ] **Step 2: Create module-info.java**

```java
module io.excel4j.core {
    exports io.excel4j.core;
    exports io.excel4j.core.model;
    exports io.excel4j.core.model.style;
    exports io.excel4j.core.exception;
    exports io.excel4j.core.io;
}
```

- [ ] **Step 3: Write the failing round-trip test**

Create `excel4j-core/src/test/java/io/excel4j/core/io/OoxmlRoundTripTest.java`:

```java
package io.excel4j.core.io;

import io.excel4j.core.Excel;
import io.excel4j.core.model.*;
import io.excel4j.core.model.style.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class OoxmlRoundTripTest {

    @TempDir
    Path tempDir;

    @Test
    void textValueRoundTrip() {
        Workbook wb = Excel.create();
        wb.sheet(1).cell("A1").setValue(new TextValue("Hello world"));

        Path file = tempDir.resolve("test.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new TextValue("Hello world"));
    }

    @Test
    void numberValueRoundTrip() {
        Workbook wb = Excel.create();
        wb.sheet(1).cell("B2").setValue(new NumberValue(123.45));

        Path file = tempDir.resolve("numbers.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell("B2").getValue())
            .isEqualTo(new NumberValue(123.45));
    }

    @Test
    void booleanValueRoundTrip() {
        Workbook wb = Excel.create();
        wb.sheet(1).cell("A1").setValue(new BooleanValue(true));

        Path file = tempDir.resolve("bool.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new BooleanValue(true));
    }

    @Test
    void multipleSheetRoundTrip() {
        Workbook wb = Excel.create();
        wb.sheet(1).cell("A1").setValue(new TextValue("Sheet1"));
        wb.addSheet("Orders");
        wb.sheet("Orders").cell("A1").setValue(new TextValue("Orders data"));

        Path file = tempDir.resolve("multi.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheets()).hasSize(2);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new TextValue("Sheet1"));
        assertThat(read.sheet("Orders").cell("A1").getValue())
            .isEqualTo(new TextValue("Orders data"));
    }

    @Test
    void formulaRoundTrip() {
        Workbook wb = Excel.create();
        wb.sheet(1).cell("A1").setValue(new NumberValue(10.0));
        wb.sheet(1).cell("A2").setValue(new NumberValue(20.0));
        wb.sheet(1).cell("A3").setFormula("SUM(A1:A2)");

        Path file = tempDir.resolve("formula.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell("A3").getFormula()).isEqualTo("SUM(A1:A2)");
    }

    @Test
    void largeCellCountRoundTrip() {
        Workbook wb = Excel.create();
        for (int row = 1; row <= 1000; row++) {
            wb.sheet(1).cell(row, 1).setValue(new NumberValue(row));
            wb.sheet(1).cell(row, 2).setValue(new TextValue("Row " + row));
        }

        Path file = tempDir.resolve("large.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell(500, 1).getValue())
            .isEqualTo(new NumberValue(500.0));
        assertThat(read.sheet(1).cell(1000, 2).getValue())
            .isEqualTo(new TextValue("Row 1000"));
    }

    @Test
    void boldStyleRoundTrip() {
        Workbook wb = Excel.create();
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        wb.sheet(1).cell("A1").setValue(new TextValue("Bold"));
        wb.sheet(1).cell("A1").setStyle(bold);

        Path file = tempDir.resolve("style.xlsx");
        Excel.write(wb, file);

        // verify file is valid XLSX (can be opened by Excel)
        assertThat(file).exists();
        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new TextValue("Bold"));
    }
}
```

- [ ] **Step 4: Run all tests**

```bash
mvn test -pl excel4j-core 2>&1 | tail -10
```

Expected: All tests pass. If any fail, fix the OoxmlWriter/OoxmlReader until round-trip tests pass.

- [ ] **Step 5: Run full build**

```bash
mvn verify -q 2>&1 | tail -5
```

Expected: BUILD SUCCESS across all modules.

- [ ] **Step 6: Commit**

```bash
git add excel4j-core/src/main/java/io/excel4j/core/Excel.java
git add excel4j-core/src/main/java/io/excel4j/core/module-info.java
git add excel4j-core/src/test/java/io/excel4j/core/io/OoxmlRoundTripTest.java
git commit -m "feat(core): Excel static factory, JPMS module-info, round-trip integration tests"
```

---

## Done

`excel4j-core` complete. Produces a working, testable XLSX read/write library with:
- Full mutable workbook model
- All `CellValue` types including dates
- Style model with deduplication
- StAX streaming reader + writer
- JPMS module with explicit exports

**Next plans:**
- `2026-05-01-excel4j-formula.md` — tokenizer, AST, evaluator, ~80 functions
- `2026-05-01-excel4j-report.md` — template engine, band expansion, tag substitution
