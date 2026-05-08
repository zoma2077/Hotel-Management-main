package com.cse241.hotel.interfaces;

import com.cse241.hotel.enums.PaymentMethod;
import com.cse241.hotel.exceptions.InvalidPaymentException;
import com.cse241.hotel.model.transaction.Invoice;

public interface Payable {
    void pay(Invoice invoice, PaymentMethod method) throws InvalidPaymentException;
}

