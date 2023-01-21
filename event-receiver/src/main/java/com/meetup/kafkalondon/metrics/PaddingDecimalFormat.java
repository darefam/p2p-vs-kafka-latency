package com.meetup.kafkalondon.metrics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;

@SuppressWarnings("serial")
public class PaddingDecimalFormat extends DecimalFormat {
    private final int minimumLength;

    public PaddingDecimalFormat(String pattern, int minLength) {
        super(pattern);
        minimumLength = minLength;
    }

    public PaddingDecimalFormat(String pattern, DecimalFormatSymbols symbols, int minLength) {
        super(pattern, symbols);
        minimumLength = minLength;
    }

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        int initLength = toAppendTo.length();
        super.format(number, toAppendTo, pos);
        return pad(toAppendTo, initLength);
    }

    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        int initLength = toAppendTo.length();
        super.format(number, toAppendTo, pos);
        return pad(toAppendTo, initLength);
    }

    private StringBuffer pad(StringBuffer toAppendTo, int initLength) {
        int numLength = toAppendTo.length() - initLength;
        int padLength = minimumLength - numLength;
        if (padLength > 0) {
            StringBuffer pad = new StringBuffer(padLength);
            for (int i = 0; i < padLength; i++) {
                pad.append(' ');
            }
            toAppendTo.insert(initLength, pad);
        }
        return toAppendTo;
    }
}
