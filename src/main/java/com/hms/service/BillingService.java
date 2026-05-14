package com.hms.service;

import com.hms.dao.BillDao;
import com.hms.model.Bill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class BillingService {

    @Autowired
    private BillDao billDao;

    public Bill generateBill(Bill bill) {
        if (bill.getTotalAmount() == null || bill.getTotalAmount().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Total amount must be non-negative");
        if (bill.getBillDate() == null) bill.setBillDate(LocalDate.now());
        if (bill.getPaymentStatus() == null) bill.setPaymentStatus(Bill.PaymentStatus.UNPAID);
        return billDao.save(bill);
    }

    public Bill getBill(int id) {
        return billDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + id));
    }

    public List<Bill> getAllBills() {
        return billDao.findAll();
    }

    public List<Bill> getBillsByPatient(int patientId) {
        return billDao.findByPatient(patientId);
    }

    public Bill processPayment(int billId) {
        Bill bill = getBill(billId);
        if (bill.getPaymentStatus() == Bill.PaymentStatus.PAID)
            throw new IllegalStateException("Bill is already paid");
        bill.setPaymentStatus(Bill.PaymentStatus.PAID);
        return billDao.update(bill);
    }

    public Bill update(Bill bill) {
        getBill(bill.getBillId());
        return billDao.update(bill);
    }

    public void deleteBill(int id) {
        billDao.delete(id);
    }
}
