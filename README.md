# ProcureGov Tender Management System - Setup Guide

## Prerequisites
- Apache Tomcat/8.5.96
- MySQL 5.7+ with DB `tlotlisangkhutlang2333874`
- NetBeans IDE or deploy WAR to Tomcat

## Database Setup
1. Import `tlotlisangkhutlang2333874.sql` to MySQL DB `tlotlisangkhutlang2333874`
2. Update JNDI creds in `META-INF/context.xml` (username/password)
3. Seed users:
   - Procurement Officer: peter.mokhosi / password123 (hash it)
   - Evaluation Committee: thabo.masilo / password123
   - Supplier: Karabo / Password123 (reg new)

## Deploy
1. NetBeans: Run → Run Project (Tomcat server)
2. Or build WAR (right-click → Clean&Build), deploy to Tomcat webapps/

## Upload Dir
Create `procuregov-uploads` in Tomcat base dir.

## Test
- Login peter.mokhosi / password123 → Tender List
- Register supplier → login → dashboard
- Note: Some servlets stub (TenderList etc.), auth works.

## Roles
- SUPPLIER: Bid
- PROCUREMENT_OFFICER: Manage tenders
- EVALUATION_COMMITTEE: Score bids

Enjoy ProcureGov!
