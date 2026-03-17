# SF Explorer - A San Francisco City Guide

**SF Explorer** is a Java-based interactive application designed to serve as a comprehensive guide to San Francisco. By merging software engineering principles with user-centered design, this project provides a data-driven experience for exploring dining, attractions, and local events.

---

## 🚀 Project Overview
The application aims to showcase technical proficiency in Java while providing a practical utility for residents and tourists alike. Key highlights include:
* **Interactive Design**: A Java Swing GUI featuring categorized tabs such as "Eat & Drink," "Attractions," "Events," and "Outdoor Activities".
* **Real-Time Data**: Integration with remote servers and APIs to fetch live details on restaurant ratings, cuisine types, and upcoming exhibitions.
* **Performance & Security**: Utilization of multithreading for responsive data loading and encrypted credential management for user security.

---

## 🛠️ Technical Components
The project architecture leverages several core Java technologies and frameworks:

| Component | Technology / Library | Purpose |
| :--- | :--- | :--- |
| **GUI** | Java Swing | Interface for browsing and searching city locations. |
| **Networking** | Java Sockets / HTTPClient |Fetching live data from online APIs. |
| **Database** | MySQL or SQLite + JDBC | Storing user favorites and preferences. |
| **Multithreading** | Java Threads / Executors | Handling background data loading and UI responsiveness. |
| **Framework** | Spring Boot | Dependency injection, modular design, and scalability. |

---

## ✨ Core Features
* **Search & Explore**: Users can search by keyword or category and click items to view detailed descriptions and ratings.
* **Live Integration**: Retrieves restaurant names, addresses for navigation, and event schedules via HTTP requests.
* **Personalized Favorites**: Features a system where favorite locations are saved to a local database using JDBC-based CRUD operations.
* **Concurrent Processing**: Employs worker threads to process multiple API requests simultaneously without freezing the interface.
* **Secure Authentication**: User login information is protected using encryption with salt and pepper.

---

## 🏁 Getting Started

### Prerequisites
* **Java Development Kit (JDK)**: Required to run the Swing application and Spring Boot framework.
* **Database**: Access to a local **SQLite** or **MySQL** instance.
* **Network Access**: Required for the application to reach remote servers and retrieve live data.

### Installation & Execution
1. **Clone the Repository**: Download the source code to your local machine.
2. **Database Setup**: 
    * Ensure your MySQL or SQLite environment is active.
    * The application uses **JDBC** to establish connectivity for storing favorites.
3. **Dependency Management**: Use **Spring Boot** to handle necessary libraries and modular structure.
4. **Run**: Launch the application to be greeted by the main dashboard.

### Sample User Flow
1. Select a category like **"Food & Drinks"**.
2. The app fetches data in the background via HTTP requests in worker threads.
3. View results in a scrollable list with ratings and locations.
4. Click an entry to **"Add to Favorites"**.
5. Access your stored favorites later from the local database.

---

**Author:** Armish Javed 
