package com.xiaoma.mylibrary;


import com.xiaoma.annotation.router.FullUrl;
import com.xiaoma.annotation.router.IntentExtrasParam;

public interface RouterService {

    @FullUrl("router://com.xiaoma.mylibrary.libraryactivity")
    void startLibraryActivity(@IntentExtrasParam("stringParam") String stringParam,
                              @IntentExtrasParam("user") User user);
}
