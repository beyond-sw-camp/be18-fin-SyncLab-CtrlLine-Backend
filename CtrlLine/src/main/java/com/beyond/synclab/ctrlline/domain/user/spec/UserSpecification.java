package com.beyond.synclab.ctrlline.domain.user.spec;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserPosition;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;
import java.time.LocalDate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class UserSpecification {
    public Specification<Users> userDepartmentContains(String dept) {
        return (root, query, cb) ->
            dept == null || dept.isEmpty()
                ? null
                : cb.like(root.get("department"), "%" + dept + "%");
    }

    public Specification<Users> userStatusEquals(UserStatus status) {
        return (root, query, cb) ->
            status == null
                ? null
                : cb.equal(root.get("status"), status);
    }

    public Specification<Users> userRoleEquals(UserRole role) {
        return (root, query, cb) ->
            role == null
                ? null
                : cb.equal(root.get("role"), role);
    }

    public Specification<Users> userPositionEquals(UserPosition userPosition) {
        return (root, query, cb) ->
            userPosition == null
                ? null
                : cb.equal(root.get("position"), userPosition);
    }

    public Specification<Users> userPhoneNumberContains(String phone) {
        return (root, query, cb) ->
            phone == null || phone.isEmpty()
                ? null
                : cb.like(root.get("phoneNumber"), "%" + phone + "%");
    }

    public Specification<Users> userEmpNoContains(String empNo) {
        return (root, query, cb) ->
            empNo == null || empNo.isBlank()
                ? null
                : cb.like(root.get("empNo"), "%" + empNo + "%");
    }

    public Specification<Users> userNameContains(String name) {
        return (root, query, cb) ->
            name == null || name.isBlank()
                ? null
                : cb.like(root.get("name"), "%" + name + "%");
    }

    public Specification<Users> userEmailContains(String email) {
        return (root, query, cb) ->
            email == null || email.isBlank()
            ? null
            : cb.like(root.get("email"), "%" + email + "%");
    }

    public Specification<Users> userHiredDateAfter(LocalDate date) {
        return (root, query, cb) ->
            date == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("hiredDate"), date);
    }

    public Specification<Users> userTerminationDateBefore(LocalDate date) {
        return (root, query, cb) ->
            date == null
                ? null
                : cb.lessThanOrEqualTo(root.get("terminationDate"), date);
    }
}
