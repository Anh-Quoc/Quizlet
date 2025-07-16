package com.prm.quizlet.library;

import com.prm.quizlet.entity.Sets;

public class SetItem implements LibraryItem {
    public Sets set;
    public int flashcardCount;
    public SetItem(Sets set, int flashcardCount) {
        this.set = set;
        this.flashcardCount = flashcardCount;
    }
}
