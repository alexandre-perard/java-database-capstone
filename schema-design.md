# Databases Design

## MySQL Database Design

### Table: patients

- id: INT, Primary Key, Auto Increment
- username: STRING, Unique, Not Null
- password: STRING, Not Null
- first_name: STRING, Not Null
- last_name: STRING, Not Null

### Table: doctors

- id: INT, Primary Key, Auto Increment
- username: STRING, Unique, Not Null
- password: STRING, Not Null
- first_name: STRING, Not Null
- last_name: STRING, Not Null
- speciality: STRING, Not Null

### Table: appointments

- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled), Not Null

### Table: admin

- id: INT, Primary Key, Auto Increment
- username: STRING, Unique, Not Null
- password: STRING, Not Null


### Table: unavailable_timeslot

Indicates that a doctor is not availbale during a given timeslot

- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- start: DATETIME, Not Null
- end: DATETIME, Not Null

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  }
}
```
