# UVA Bus Routes Database Project

## Overview

This project is a comprehensive Java-based application designed to build and manage a database of bus routes for the University Transit Service (UTS) at the University of Virginia. The primary goal of this project is to develop a robust database that stores and organizes information about bus lines, stops, and routes, allowing for efficient querying and data retrieval. This project demonstrates proficiency in database design, Java programming, API integration, and SQL query writing.

## Key Features

### 1. API Integration and Data Parsing
   - Utilized the UVA Devhub API to retrieve bus route information in JSON format.
   - Implemented Java classes to parse this JSON data into a structured list of objects representing bus lines, stops, and routes.

### 2. Database Design and Implementation
   - Designed an SQLite database to store bus routes data efficiently.
   - Created tables for storing bus lines, stops, and routes with appropriate relationships and constraints.
   - Implemented CRUD (Create, Read, Update, Delete) operations using JDBC for database interaction.

### 3. Efficient Data Storage
   - Implemented methods to store and retrieve data from the SQLite database, ensuring data integrity and performance.
   - Ensured data is inserted in an order that reflects real-world bus routes.

### 4. Querying and Data Analysis
   - Developed methods to query the database for specific bus lines, stops, and routes.
   - Implemented an algorithm to find the closest bus stop to a given location using Euclidean distance.
   - Created a method to recommend the best bus line between two stops based on the shortest route distance.

### 5. Error Handling and Transaction Management
   - Implemented robust error handling to ensure the application remains stable during database operations.
   - Managed database transactions to ensure data consistency, with rollbacks on failures.

### 6. Database Persistence and Submission
   - Ensured that the SQLite database is correctly populated and persistent, with all required data from the API.
   - The database was included in the final submission to demonstrate the completeness of the data import.

## Technical Skills Demonstrated

- **Java Programming:** Strong understanding of Java, including object-oriented principles, exception handling, and file I/O.
- **Database Management:** Proficient in designing and implementing relational databases using SQLite. Familiarity with SQL for data querying and manipulation.
- **API Integration:** Experienced in working with RESTful APIs to retrieve and parse JSON data.
- **Data Analysis:** Developed algorithms for spatial analysis, including finding the closest points and optimal routing based on real-world constraints.
- **Problem Solving:** Applied critical thinking to design a solution that efficiently stores and retrieves complex data structures.
