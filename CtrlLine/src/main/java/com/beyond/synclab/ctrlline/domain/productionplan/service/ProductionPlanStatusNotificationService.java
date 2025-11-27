package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.common.mail.MailSendRequest;
import com.beyond.synclab.ctrlline.common.mail.MailService;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionPlanStatusNotificationService {

    private static final String PLAN_DETAIL_URL_TEMPLATE =
            "https://stage-synclab-ctrlline.vercel.app/production-management/production-plans/%d";

    private final UserRepository userRepository;
    private final MailService mailService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void notifyStatusChange(ProductionPlans plan, PlanStatus previousStatus) {
        if (plan == null || previousStatus == null) {
            return;
        }

        PlanStatus currentStatus = plan.getStatus();
        if (currentStatus == null || currentStatus == previousStatus) {
            log.debug("생산계획 상태 변경 없음. documentNo={}, status={}", plan.getDocumentNo(), currentStatus);
            return;
        }

        List<Users> receivers = resolveReceivers(plan);
        if (receivers.isEmpty()) {
            log.debug("생산계획 상태 변경 알림 수신자 없음. documentNo={}", plan.getDocumentNo());
            return;
        }

        String documentNo = plan.getDocumentNo();
        String subject = String.format("[CtrlLine] 생산계획 %s 상태 변경 안내", documentNo);
        String body = String.format(
                "생산계획 %s의 상태가 \"%s\" 에서 \"%s\"(으)로 변경되었습니다.",
                documentNo,
                previousStatus,
                currentStatus
        );

        log.info("생산계획 상태 변경 알림 준비 documentNo={}, from={} to={}, receivers={}",
                documentNo, previousStatus, currentStatus, receivers.size());
        sendMail(receivers, subject, body, plan);
    }

    public void notifyScheduleChange(ProductionPlans plan, LocalDateTime previousStart, LocalDateTime previousEnd) {
        if (plan == null) {
            return;
        }

        LocalDateTime newStart = plan.getStartTime();
        LocalDateTime newEnd = plan.getEndTime();

        if (!hasScheduleChanged(previousStart, newStart) && !hasScheduleChanged(previousEnd, newEnd)) {
            log.debug("생산계획 일정 변경 없음. documentNo={}", plan.getDocumentNo());
            return;
        }

        List<Users> receivers = resolveReceivers(plan);
        if (receivers.isEmpty()) {
            log.debug("생산계획 일정 변경 알림 수신자 없음. documentNo={}", plan.getDocumentNo());
            return;
        }

        String documentNo = plan.getDocumentNo();
        String subject = String.format("[CtrlLine] 생산계획 %s 일정 변경 안내", documentNo);

        String formattedPrevStart = formatDateTime(previousStart).orElse("미정");
        String formattedPrevEnd = formatDateTime(previousEnd).orElse("미정");
        String formattedNewStart = formatDateTime(newStart).orElse("미정");
        String formattedNewEnd = formatDateTime(newEnd).orElse("미정");

        String body = String.format(
                "\"%s\" 생산계획의 일정이 %s ~ %s 에서 %s ~ %s (으)로 변경되었습니다.",
                documentNo,
                formattedPrevStart,
                formattedPrevEnd,
                formattedNewStart,
                formattedNewEnd
        );

        log.info("생산계획 일정 변경 알림 준비 documentNo={}, receivers={}", documentNo, receivers.size());
        sendMail(receivers, subject, body, plan);
    }

    private void sendMail(List<Users> receivers, String subject, String body, ProductionPlans plan) {
        receivers.stream()
                .map(Users::getEmail)
                .filter(StringUtils::hasText)
                .distinct()
                .forEach(email -> mailService.send(MailSendRequest.text(
                        email,
                        subject,
                        bodyWithLink(body, plan)
                )));
    }

    private List<Users> resolveReceivers(ProductionPlans plan) {
        Set<Long> receiverIds = new LinkedHashSet<>();
        if (plan.getSalesManagerId() != null) {
            receiverIds.add(plan.getSalesManagerId());
        }
        if (plan.getProductionManagerId() != null) {
            receiverIds.add(plan.getProductionManagerId());
        }

        if (receiverIds.isEmpty()) {
            log.warn("생산계획 알림 수신 대상이 없습니다. documentNo={}", plan.getDocumentNo());
            return List.of();
        }

        List<Users> receivers = userRepository.findAllById(receiverIds);
        if (receivers.isEmpty()) {
            log.warn("생산계획 알림 수신자로 등록된 사용자를 찾을 수 없습니다. documentNo={}", plan.getDocumentNo());
            return List.of();
        }

        return receivers;
    }

    private String bodyWithLink(String baseBody, ProductionPlans plan) {
        Long id = plan != null ? plan.getId() : null;
        if (id == null) {
            return baseBody;
        }
        String detail = PLAN_DETAIL_URL_TEMPLATE.formatted(id);
        return baseBody + System.lineSeparator() + System.lineSeparator() + "상세보기: " + detail;
    }

    private boolean hasScheduleChanged(LocalDateTime previous, LocalDateTime current) {
        if (previous == null && current == null) {
            return false;
        }
        if (previous == null || current == null) {
            return true;
        }
        return !previous.equals(current);
    }

    private Optional<String> formatDateTime(LocalDateTime value) {
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value.format(DATE_TIME_FORMATTER));
    }
}
