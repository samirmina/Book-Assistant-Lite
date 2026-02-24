package com.bookassistant.model;

import java.util.List;

public record BookData(String fileName, List<PageData> pages, String fullText) {
}

