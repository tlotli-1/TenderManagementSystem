# PROCUREGOV - Tender Management System

## Ministry of Public Works, Kingdom of Lesotho

---

## Project Overview

ProcureGov is a web-based Tender Management System that digitises the full tender lifecycle for the Ministry of Public Works of the Kingdom of Lesotho. The system allows the Ministry to publish tenders, receive sealed electronic bids from registered suppliers, conduct structured evaluation using a weighted scoring model, and formally award contracts through a secure, role-controlled web portal.

---

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | JDK 8 | Backend programming language |
| Apache Tomcat | 8.5.96 | Web application server |
| MySQL | 5.7+ / 8.0 | Relational database |
| JSP | 2.3+ | View layer |
| JSTL | 1.2 | Tag library for JSP |
| JavaMail | 1.6.2 | Email notifications |
| HTML/CSS | - | Frontend styling |

---

## System Requirements

### Minimum Requirements

- **Operating System**: Windows 10/11, Linux, or macOS
- **Java**: JDK 8 or higher
- **Tomcat**: Apache Tomcat 8.5.x or 9.x
- **MySQL**: MySQL 5.7 or higher (or MariaDB 10.4+)
- **RAM**: 4GB minimum (8GB recommended)
- **Disk Space**: 500MB free space

### Required JAR Files (in `WEB-INF/lib`)

| JAR File | Version | Purpose |
|----------|---------|---------|
| `mysql-connector-java-8.0.33.jar` | 8.0.33 | MySQL database connection |
| `jstl-1.2.jar` | 1.2 | JSP tag library |
| `javax.mail-1.6.2.jar` | 1.6.2 | Email notifications |
| `activation-1.1.1.jar` | 1.1.1 | JavaMail dependency |

---

## Installation Guide

### Step 1: Install Prerequisites

1. **Install JDK 11** from [Oracle](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
2. **Install XAMPP** (includes Apache Tomcat and MySQL) from [Apache Friends](https://www.apachefriends.org/)
3. **Verify installations**:
   ```cmd
   java -version
