/*******************************************************************************
 * Copyright 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * Copyright 2011 GenFTW contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.turbomanage.storm.apt;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

/**
 * Simple logging facade for JSR-269 {@link Messager}.
 */
public class ProcessorLogger {

    private final Messager messager;

    public ProcessorLogger(Messager messager) {
        this.messager = messager;
    }

    void log(Kind kind, String msg, Element elm) {
        if (elm != null) {
            messager.printMessage(kind, msg, elm);
        } else {
            messager.printMessage(kind, msg);
        }
    }

    public void info(String msg, Element elm) {
        log(Kind.NOTE, msg, elm);
    }

    public void info(String msg) {
        info(msg, null);
    }

    public void warning(String msg, Element elm) {
        log(Kind.WARNING, msg, elm);
    }

    public void warning(String msg) {
        warning(msg, null);
    }

    public void error(String msg, Exception ex, Element elm) {
        log(Kind.ERROR, formatErrorMessage(msg, ex), elm);

        // Ensure that error messages appear on standard error output
        System.err.println(formatErrorMessage(msg, ex));
    }

    public void error(String msg, Element elm) {
        error(msg, null, elm);
    }

    public void error(String msg, Exception ex) {
    		log(Kind.ERROR, formatErrorMessage(msg, ex), null);
    }

    public String formatErrorMessage(String msg, Exception ex) {
        StringBuilder sb = new StringBuilder(msg);

        if (ex != null) {
            sb.append(": ").append(ex.getClass().getCanonicalName());
            sb.append(": ").append(ex.getLocalizedMessage());
        }

        return sb.toString();
    }

}
