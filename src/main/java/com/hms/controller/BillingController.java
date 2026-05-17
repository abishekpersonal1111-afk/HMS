package com.hms.controller;

import com.hms.model.Bill;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.service.BillingService;
import com.hms.util.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @GetMapping
    public ResponseEntity<?> getAll(HttpSession session, @RequestParam(required = false) Integer patientId) {
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() == User.Role.ADMIN) {
                var result = patientId != null
                        ? billingService.getBillsByPatient(patientId)
                        : billingService.getAllBills();
                return ResponseEntity.ok(ApiResponse.ok(result));
            }
            if (currentUser.getRole() == User.Role.PATIENT) {
                return ResponseEntity
                        .ok(ApiResponse.ok(billingService.getBillsByPatient(currentUser.getPatient().getPatientId())));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("Only admins and patients can view bills"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") int id, HttpSession session) {
        try {
            Bill bill = billingService.getBill(id);
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() == User.Role.ADMIN) {
                return ResponseEntity.ok(ApiResponse.ok(bill));
            }
            if (currentUser.getRole() == User.Role.PATIENT) {
                if (currentUser.getPatient() == null
                        || !currentUser.getPatient().getPatientId().equals(bill.getPatient().getPatientId()))
                    throw new RuntimeException("Access denied: patients can only view their own bills");
                return ResponseEntity.ok(ApiResponse.ok(bill));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("Only admins and patients can view bills"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> generate(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() != User.Role.ADMIN)
                throw new RuntimeException("Only admins can generate bills");

            Bill bill = new Bill();
            Patient p = new Patient();
            p.setPatientId((Integer) body.get("patientId"));
            bill.setPatient(p);
            bill.setTotalAmount(new BigDecimal(body.get("totalAmount").toString()));
            bill.setBillDate(LocalDate.now());
            bill.setPaymentStatus(Bill.PaymentStatus.UNPAID);
            return ResponseEntity.ok(ApiResponse.ok("Bill generated", billingService.generateBill(bill)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<?> pay(@PathVariable("id") int id, HttpSession session) {
        try {
            Bill bill = billingService.getBill(id);
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() == User.Role.ADMIN) {
                return ResponseEntity.ok(ApiResponse.ok("Payment processed", billingService.processPayment(id)));
            }
            if (currentUser.getRole() == User.Role.PATIENT) {
                if (currentUser.getPatient() == null
                        || !currentUser.getPatient().getPatientId().equals(bill.getPatient().getPatientId()))
                    throw new RuntimeException("Access denied: patients can only pay their own bills");
                return ResponseEntity.ok(ApiResponse.ok("Payment processed", billingService.processPayment(id)));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("Only admins and patients can process payments"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") int id, HttpSession session) {
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() != User.Role.ADMIN)
                throw new RuntimeException("Only admins can delete bills");
            billingService.deleteBill(id);
            return ResponseEntity.ok(ApiResponse.ok("Bill deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null)
            throw new RuntimeException("Unauthorized");
        return user;
    }
}
