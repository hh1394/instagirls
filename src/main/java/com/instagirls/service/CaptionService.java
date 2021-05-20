package com.instagirls.service;

import org.springframework.stereotype.Service;

@Service
public class CaptionService {

    public String getCaption() {
        final Forismatic.Quote quote = new Forismatic().getQuote();
        return String.format("\"%s\" - %s", quote.getQuoteText(), quote.getQuoteAuthor());
    }

}
