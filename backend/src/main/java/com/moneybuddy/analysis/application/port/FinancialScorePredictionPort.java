package com.moneybuddy.analysis.application.port;

import com.moneybuddy.analysis.api.AnalisisFinancieroRequest;
import java.util.OptionalInt;

public interface FinancialScorePredictionPort {

	OptionalInt predictScore(AnalisisFinancieroRequest request);
}
