package com.minirag.pipeline;

import com.minirag.model.Context;

public interface AnswerAgent {
    // Takes the entire context, generates the response, writes it to the Context, or returns a string.
    String answer(Context context);
}