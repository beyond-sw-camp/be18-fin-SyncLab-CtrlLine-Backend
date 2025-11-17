package com.beyond.synclab.ctrlline.domain.user.entity;

import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;
import com.beyond.synclab.ctrlline.domain.user.dto.UserUpdateRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "`user`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(EntityActionLogger.class)
@EqualsAndHashCode(of = "id")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long id;

    @Column(name = "emp_no", nullable = false, unique = true)
    private String empNo; // 입사연도(4)월(2)순번(3)

    @Column(name = "user_name", nullable = false)
    private String name;

    @Column(name = "user_email", nullable = false, unique = true)
    private String email;

    @Column(name = "user_password", nullable = false)
    private String password;

    @Column(name = "user_phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "user_hired_date", nullable = false)
    private LocalDate hiredDate;

    @Column(name = "user_termination_date")
    private LocalDate terminationDate;

    @Column(name = "user_extension")
    private String extension;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role; // USER, EMPLOYEE, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus status; // ACTIVE, LEAVE, RETIRED

    @Column(name = "user_department", nullable = false)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_position", nullable = false)
    private UserPosition position; // ASSISTANT, MANAGER, ...

    @Column(name = "user_address", nullable = false)
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private <T> void updateIfPresent(T value, Consumer<T> consumer) {
        if (value == null) return;
        if (value instanceof String s && s.isBlank()) return;
        consumer.accept(value);
    }

    public void update(UserUpdateRequestDto dto, String password) {
        updateIfPresent(dto.getDepartment(), v -> this.department = v);
        updateIfPresent(dto.getPosition(), v -> this.position = v);
        updateIfPresent(dto.getEmail(), v -> this.email = v);
        updateIfPresent(dto.getName(), v -> this.name = v);
        updateIfPresent(dto.getRole(), v -> this.role = v);
        updateIfPresent(dto.getPhoneNumber(), v -> this.phoneNumber = v);
        updateIfPresent(password, v -> this.password = v);
        updateIfPresent(dto.getStatus(), v -> this.status = v);
        updateIfPresent(dto.getAddress(), v -> this.address = v);
        updateIfPresent(dto.getTerminationDate(), v -> this.terminationDate = v);
        updateIfPresent(dto.getExtension(), v -> this.extension = v);
    }

    public boolean isAdminRole() {
        return this.role.equals(UserRole.ADMIN);
    }

    public boolean isUserRole() {
        return this.role.equals(UserRole.USER);
    }

    public boolean isManagerRole() {
        return this.role.equals(UserRole.MANAGER);
    }

    public enum UserRole {
        ADMIN, MANAGER, USER
    }

    public enum UserStatus {
        ACTIVE, LEAVE, RESIGNED
    }

    public enum UserPosition {
        ASSISTANT, ASSISTANT_MANAGER, MANAGER, GENERAL_MANAGER, DIRECTOR, CEO
    }
}
