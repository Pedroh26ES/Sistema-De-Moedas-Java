package br.com.sistemamoedas.controller;

import br.com.sistemamoedas.service.RegraNegocioException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RegraNegocioExceptionMapper implements ExceptionMapper<RegraNegocioException> {

    @Override
    public Response toResponse(RegraNegocioException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiController.ErrorDto(exception.getMessage()))
                .build();
    }
}
