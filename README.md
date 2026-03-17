# SF Explorer - A San Francisco City Guide

[cite_start]**SF Explorer** is a Java-based interactive application designed to serve as a comprehensive guide to San Francisco[cite: 3, 6]. [cite_start]By merging software engineering principles with user-centered design, this project provides a data-driven experience for exploring dining, attractions, and local events[cite: 7, 52].

---

## 🚀 Project Overview
[cite_start]The application aims to showcase technical proficiency in Java while providing a practical utility for residents and tourists alike[cite: 12, 51]. Key highlights include:
* [cite_start]**Interactive Design**: A Java Swing GUI featuring categorized tabs such as "Eat & Drink," "Attractions," "Events," and "Outdoor Activities"[cite: 14, 20, 21].
* [cite_start]**Real-Time Data**: Integration with remote servers and APIs to fetch live details on restaurant ratings, cuisine types, and upcoming exhibitions[cite: 15, 25, 26, 27].
* [cite_start]**Performance & Security**: Utilization of multithreading for responsive data loading and encrypted credential management for user security[cite: 17, 32].

---

## 🛠️ Technical Components
[cite_start]The project architecture leverages several core Java technologies and frameworks[cite: 8, 39]:

| Component | Technology / Library | Purpose |
| :--- | :--- | :--- |
| **GUI** | Java Swing | [cite_start]Interface for browsing and searching city locations[cite: 40]. |
| **Networking** | Java Sockets / HTTPClient | [cite_start]Fetching live data from online APIs[cite: 40]. |
| **Database** | MySQL or SQLite + JDBC | [cite_start]Storing user favorites and preferences[cite: 40]. |
| **Multithreading** | Java Threads / Executors | [cite_start]Handling background data loading and UI responsiveness[cite: 41]. |
| **Framework** | Spring Boot | [cite_start]Dependency injection, modular design, and scalability[cite: 38, 41]. |

---

## ✨ Core Features
* [cite_start]**Search & Explore**: Users can search by keyword or category and click items to view detailed descriptions and ratings[cite: 22, 23].
* [cite_start]**Live Integration**: Retrieves restaurant names, addresses for navigation, and event schedules via HTTP requests[cite: 26, 28, 29].
* [cite_start]**Personalized Favorites**: Features a system where favorite locations are saved to a local database using JDBC-based CRUD operations[cite: 23, 31].
* [cite_start]**Concurrent Processing**: Employs worker threads to process multiple API requests simultaneously without freezing the interface[cite: 34, 35, 36].
* [cite_start]**Secure Authentication**: User login information is protected using encryption with salt and pepper[cite: 32].

---

## 🏁 Getting Started

### Prerequisites
* [cite_start]**Java Development Kit (JDK)**: Required to run the Swing application and Spring Boot framework[cite: 1, 38].
* [cite_start]**Database**: Access to a local **SQLite** or **MySQL** instance[cite: 31].
* [cite_start]**Network Access**: Required for the application to reach remote servers and retrieve live data[cite: 11, 15].

### Installation & Execution
1. **Clone the Repository**: Download the source code to your local machine.
2. **Database Setup**: 
    * [cite_start]Ensure your MySQL or SQLite environment is active[cite: 31, 40].
    * [cite_start]The application uses **JDBC** to establish connectivity for storing favorites[cite: 31].
3. [cite_start]**Dependency Management**: Use **Spring Boot** to handle necessary libraries and modular structure[cite: 38].
4. [cite_start]**Run**: Launch the application to be greeted by the main dashboard[cite: 43].

### Sample User Flow
1. [cite_start]Select a category like **"Food & Drinks"**[cite: 44].
2. [cite_start]The app fetches data in the background via HTTP requests in worker threads[cite: 45].
3. [cite_start]View results in a scrollable list with ratings and locations[cite: 46].
4. [cite_start]Click an entry to **"Add to Favorites"**[cite: 47].
5. [cite_start]Access your stored favorites later from the local database[cite: 48].

---

[cite_start]**Author:** Armish Javed (aj4513) [cite: 4]  
[cite_start]**Course:** Intro to Java - Final Project Proposal [cite: 1, 2]