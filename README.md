# 💱 Currency Exchange Application

## 📌 Overview

This project is a backend-based currency exchange application that allows users to simulate financial transactions using a demo account. The system supports currency conversion, transaction tracking, and data visualization.

The application is designed as a simplified financial system with a focus on backend architecture, API integration, and data management.

---

## 🚀 Features

* User account system (demo/sandbox mode)
* Currency conversion functionality
* Transaction history tracking
* Database integration for users and transactions
* API-based exchange rate integration (with fallback support)
* Data visualization (line chart and candlestick chart)

---

## 🧠 System Design

The application is structured as a backend service with the following components:

* **Backend:** Java
* **Database:** (add your DB here, e.g. MySQL / PostgreSQL)
* **API Integration:** External currency exchange API
* **Data Handling:** Transaction storage and processing

The system includes a **fallback mechanism**:
If the external API is unavailable, the application switches to a demo mode to maintain functionality.

---

## 🔐 Demo Mode

The application uses a **sandbox (demo) account** that allows users to simulate currency exchange operations without real financial risk.

This approach ensures:

* safe testing environment
* consistent system behavior
* no dependency on real financial services

---

## 📊 Data Visualization

The system provides:

* Line chart for currency trends
* Candlestick chart for simulated market behavior

(Currently, visualization is based on simulated data, with potential extension to real-time data integration.)

---

## 🔗 API Integration

The application is designed to work with external currency exchange APIs to retrieve real-time exchange rates.

In case of API failure:

* the system automatically switches to demo data
* ensures stability and uninterrupted operation

---

## 🛠️ How to Run

1. Clone the repository:

```
git clone https://github.com/alfarabiajtugan1-arch/currency-app.git
```

2. Open the project in your IDE (IntelliJ IDEA / Eclipse)

3. Configure database connection (if required)

4. Run the application

---

## 🎯 Project Purpose

This project was developed as part of an academic coursework to demonstrate:

* backend development skills
* database design
* API integration
* system reliability (fallback logic)

---

## 📈 Future Improvements

* Real-time data visualization
* Improved API reliability handling
* Authentication and security (JWT)
* Frontend integration
* Advanced financial analytics

---

## 👨‍💻 Author

Alfarabi Aitugan
