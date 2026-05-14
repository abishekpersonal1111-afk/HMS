package com.hms.controller;

import com.hms.model.Bill;
import com.hms.model.Patient;
import com.hms.service.BillingService;
import com.hms.util.ApiResponse;
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
    public ResponseEntity<?> getAll(@RequestParam(required = false) Integer patientId) {
        try {
            var result = patientId != null
                    ? billingService.getBillsByPatient(patientId)
                    : billingService.getAllBills();
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(billingService.getBill(id)));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> generate(@RequestBody Map<String, Object> body) {
        try {
            Bill bill = new Bill();
            Patient p = new Patient(); p.setPatientId((Integer) body.get("patientId"));
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
    public ResponseEntity<?> pay(@PathVariable int id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Payment processed", billingService.processPayment(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            billingService.deleteBill(id);
            return ResponseEntity.ok(ApiResponse.ok("Bill deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
