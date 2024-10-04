# EXTRC_Knowledge_base_generator
## Contents

- [Project Description](#project-description)
- [Installation Requirements](#installation-requirements)
- [Build](#build)
- [Run](#run)
- [Program Control Parameters](#program-control-parameters)

## Project Description

The aim of the project is to create optimized variants of a multi-threaded pseudo-random defeasible knowledge base generator by improving the thread management system and by using the space-time trade-off optimization method.

## Installation Requirements

The following must be installed in order for the for the project to run:

- Java version: 22.0.1
- Apache Maven 3.9.8 (https://maven.apache.org/download.cgi)
- MongoDB v7.0.5

## Build

Running the following command 'mvn clean package' in the main directory will build the program package.

## Run

To run the program the following commands can be used, the first requiring direct user input of control parameters used in the defeasible knowledge base construction ,and the second storing those control paramters in file from which they will be retrieved during runtime:

-java -cp target\optimized_kbgv2-1.0-SNAPSHOT.jar extrc.App

- Get-Content ov2.txt | java -cp target\optimized_kbgv2-1.0-SNAPSHOT.jar extrc.App

## Program Control Parameters

- Generator type: ov2-generates knowledge base without using existing ones,r-reuses existing knowledge bases to generator other
- Ratio of Classical(C) to defeasible statements(D) "C:D"
- Number of ranks [int]
- Distribution [options]
- Minimum number of statements required in each rank [int]
- Number of statements (defeasible + classical) [int]
- Reuse Consequent [y/n]
- Transitive Statements in ranks [y/n]
- Antecedent (A) and Consequent (C) complexity [int] for both, "A|C"
- Select connective types [1,2,3,4,5] where 1 is disjuntion, 2 is conjunction, 3 is implication, 4 is biimplication, and 5 is all connectives.
- Choose atom character set [lowerlatin, upperlatin, altlatin, greek]
- Choose whether to print the Knowledge base generator to the terminal or not.
- Choose whether to write the Knowledge base in a text file or not.
- Choose whether to save the Knowledge base in a database or not.
- Choose whether you want to run a baserank algorithm on the Knowledge base. or not
- Choose if you want to continue generating Knowledge Bases or quit.

