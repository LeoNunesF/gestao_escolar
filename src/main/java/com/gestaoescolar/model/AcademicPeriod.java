package com.gestaoescolar.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "academic_period",
        indexes = {
                @Index(name = "idx_period_policy", columnList = "policy_id"),
                @Index(name = "idx_period_index", columnList = "indexNumber")
        })
public class AcademicPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1, 2, 3, 4...
    @Column(nullable = false)
    private Integer indexNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AcademicPolicy policy;

    @Column(length = 80, nullable = false)
    private String name;

    private LocalDate startDate;
    private LocalDate endDate;

    public Long getId() { return id; }
    public Integer getIndexNumber() { return indexNumber; }
    public void setIndexNumber(Integer indexNumber) { this.indexNumber = indexNumber; }
    public AcademicPolicy getPolicy() { return policy; }
    public void setPolicy(AcademicPolicy policy) { this.policy = policy; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}