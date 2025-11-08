package com.project.back_end.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointment")
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @NotNull
  private Doctor doctor;

  @ManyToOne(fetch = FetchType.LAZY)
  @NotNull
  private Patient patient;

  @NotNull
  @Future
  private LocalDateTime appointmentTime;

  @NotNull
  private Integer status; // 0 = scheduled, 1 = completed

  public Appointment() {
    // JPA
  }

  public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, Integer status) {
    this.doctor = doctor;
    this.patient = patient;
    this.appointmentTime = appointmentTime;
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Doctor getDoctor() {
    return doctor;
  }

  public void setDoctor(Doctor doctor) {
    this.doctor = doctor;
  }

  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public LocalDateTime getAppointmentTime() {
    return appointmentTime;
  }

  public void setAppointmentTime(LocalDateTime appointmentTime) {
    this.appointmentTime = appointmentTime;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  @Transient
  public LocalDateTime getEndTime() {
    if (appointmentTime == null) return null;
    return appointmentTime.plusHours(1);
  }

  @Transient
  public LocalDate getAppointmentDate() {
    if (appointmentTime == null) return null;
    return appointmentTime.toLocalDate();
  }

  @Transient
  public LocalTime getAppointmentTimeOnly() {
    if (appointmentTime == null) return null;
    return appointmentTime.toLocalTime();
  }

  @Override
  public String toString() {
    return "Appointment{" +
        "id=" + id +
        ", doctor=" + (doctor != null ? doctor.getId() : null) +
        ", patient=" + (patient != null ? patient.getId() : null) +
        ", appointmentTime=" + appointmentTime +
        ", status=" + status +
        '}';
  }

}

