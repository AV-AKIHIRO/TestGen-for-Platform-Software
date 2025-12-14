# Automated Test Generation for Platform Software

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/YOUR_USERNAME/pl-platform-testing/actions)
[![Java](https://img.shields.io/badge/Java-11%2B-blue)](https://www.java.com)
[![JML](https://img.shields.io/badge/spec-JML-orange)](http://www.eecs.ucf.edu/~leavens/JML/)
[![Symbolic%20Execution](https://img.shields.io/badge/engine-JPF%2FSPF-purple)](https://github.com/javapathfinder/jpf-symbc)

This repository contains the source code for the "Programming Language Support for Platform Testing" project[cite: 1]. The primary goal is to automate test input generation for Java platform software by integrating formal specifications (**JML**) with symbolic execution (**JPF/Symbolic PathFinder**).

This project solves a common problem where traditional symbolic execution tools struggle to support newer programming constructs or annotations like the Java Modeling Language (JML), leading to inefficient test generation[cite: 14].

###  Authors

* **Areen Vaghasiya** 
* **Siddharth Palod**

---

###  Project Architecture

The core of this project is a pipeline that translates high-level JML specifications into concrete, executable test cases.



The flow, as shown in the diagram above, is:

1.  **Input:** The system takes **Java source code** annotated with **JML Specs** (e.g., `@requires`, `@ensures`) and a **Test String** (a sequence of functions to call).
2.  **`genATC` (Generate Abstract Test Case):** A custom `genATC` algorithm parses the JML specs and the test string. It generates an "Abstract Test Case" (`ATC`) script. This script is a Java file that uses symbolic variables and `assume`/`assert` statements to model the JML contract.
3.  **Symbolic Execution:** The generated `ATC` file is fed into **Symbolic PathFinder (SPF)**. The SMT solver in SPF explores all possible execution paths defined by the `assume` statements.
4.  **Concrete Test Inputs:** The solver generates **Concrete Test Inputs** (e.g., `x = 10`) that satisfy the path conditions.
5.  **`CTC` (Generate Concrete Test Case):** These concrete inputs are "plugged back" into the `ATC` to create a final, runnable **Concrete Test Case** (`CTC`) that can be used to validate the original Java code.

### Core Components

* **JML Parser:** Parses JML annotations from Java source files into an Abstract Syntax Tree (AST) representation (`JmlFunctionSpec`).
* **`TestGen` Module:** Enables the test generation process, holding the `GenATC` algorithm and the `symex` wrapper.
* **`GenATC` Algorithm:** The core logic for transforming a `JmlFunctionSpec` into a function-based, self-contained test method in the `ATC` file.

* **`SpfWrapper`:** A wrapper class to interface with the JPF/Symbolic PathFinder engine, execute the `ATC` file, and capture the resulting concrete test inputs.

---

### Getting Started

*(To be filled as project progresses)*
<!-- 
#### Prerequisites

* Java 8 (or newer)
* Apache Maven
* Java PathFinder (JPF) and Symbolic PathFinder (SPF)

#### Build

```bash
# Clone the repository
git clone [https://github.com/YOUR_USERNAME/pl-platform-testing.git](https://github.com/YOUR_USERNAME/pl-platform-testing.git)
cd pl-platform-testing

# Build with Maven
mvn clean install
```

#### Run

```bash
# (Example of how it might be run)
java -jar target/pl-platform-testing.jar \
     --spec examples/library-management-system/src/main/java/com/example/lms/Stack.java \
     --test-string "PUSH_OK,POP_OK"
``` -->