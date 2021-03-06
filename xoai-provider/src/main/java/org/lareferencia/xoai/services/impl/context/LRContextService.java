/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.lareferencia.xoai.services.impl.context;

import org.lareferencia.xoai.Context;
import org.lareferencia.xoai.services.api.context.ContextService;
import org.lareferencia.xoai.services.api.context.ContextServiceException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import java.sql.SQLException;

public class LRContextService implements ContextService {
    private static final String OAI_CONTEXT = "OAI_CONTEXT";

    @Override
    public Context getContext() throws ContextServiceException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Object value = request.getAttribute(OAI_CONTEXT);
        if (value == null || !(value instanceof Context)) {
            try {
                request.setAttribute(OAI_CONTEXT, new Context());
            } catch (SQLException e) {
                throw new ContextServiceException(e);
            }
        }
        return (Context) request.getAttribute(OAI_CONTEXT);
    }
}
