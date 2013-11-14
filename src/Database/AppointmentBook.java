package Database;

public class AppointmentBook {
    static public boolean exists() {
        return false;
    }
    static public boolean hasPatients() {
        return false;
    }
    static public void createDatabase() {
        // CREATE DATABASE [AppointmentBook]
    }
    static public void createTables() {
/*
CREATE TABLE patients(
	patientId		Int				NOT NULL IDENTITY (1,1),
	MRN				VarChar(7)		NULL,
	firstName		VarChar(30)		NULL,
	lastName		VarChar(30)		NULL,
	streetAddress 	VarChar(55)		NULL,
	city			VarChar(45)		NULL,
	[state]			Char(2)			NULL,
	zipcode			VarChar(10)		NULL,
	homePhone		VarChar(25)		NULL,
	cellPhone		VarChar(25)		NULL,
	emailAddress	VarChar(100)	NULL,
	CONSTRAINT		patients_PK		PRIMARY KEY(patientId)
);

CREATE TABLE appointments(
	appointmentId	Int				NOT NULL IDENTITY (1,1),
	patientId		Int				NOT NULL,
	[appointmentDateTime]		DateTime	NULL,
	[description]	VarChar(max)			NULL,
	CONSTRAINT		appointments_PK		PRIMARY KEY(appointmentId),
	CONSTRAINT		appointments_FK		FOREIGN KEY(patientId)
						REFERENCES	patients(patientId)
							ON UPDATE  CASCADE
							ON DELETE  NO ACTION
);
         */
    }
}