package com.moneybuddy.classification.application;

import java.util.List;
import java.util.Optional;

public interface TransactionClassificationPort {

	Optional<List<String>> classify(List<TransactionClassificationInput> transactions);
}
