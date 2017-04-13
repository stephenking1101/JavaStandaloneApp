package com.ibm.rtc.automation.examples.client;

import java.util.ListIterator;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;

public class ExtendedGnuParser extends GnuParser {
	private boolean ignoreUnrecognizedOption;

    public ExtendedGnuParser(final boolean ignoreUnrecognizedOption) {
        this.ignoreUnrecognizedOption = ignoreUnrecognizedOption;
    }

    @Override
    protected void processOption(final String arg, @SuppressWarnings("rawtypes") final ListIterator iter) throws     ParseException {
        boolean hasOption = getOptions().hasOption(arg);

        if (hasOption || !ignoreUnrecognizedOption) {
            super.processOption(arg, iter);
        }
    }
}
