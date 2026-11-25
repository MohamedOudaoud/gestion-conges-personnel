<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title><c:out value="${formTitle}"/> - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <h1 class="h2 mb-4"><c:out value="${formTitle}"/></h1>

            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form action="${formAction}" method="post" class="card card-body shadow-sm">
                <c:if test="${isEdit}">
                    <input type="hidden" name="id" value="${demande.id}">
                </c:if>

                <div class="mb-3">
                    <label class="form-label" for="typeCongeId">Type de congé</label>
                    <select class="form-select" id="typeCongeId"
                            name="typeCongeId" required>
                        <option value="">Choisir un type</option>
                        <c:forEach items="${typesConge}" var="typeConge">
                            <option value="${typeConge.id}"
                                    <c:if test="${formTypeId eq typeConge.id}">selected</c:if>>
                                <c:out value="${typeConge.libelle}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label" for="dateDebut">Date de début</label>
                        <input class="form-control" id="dateDebut" name="dateDebut"
                               type="date" value="<c:out value='${formDateDebut}'/>"
                               required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label" for="dateFin">Date de fin</label>
                        <input class="form-control" id="dateFin" name="dateFin"
                               type="date" value="<c:out value='${formDateFin}'/>"
                               required>
                    </div>
                </div>

                <div class="mb-4">
                    <label class="form-label" for="motif">Motif</label>
                    <textarea class="form-control" id="motif" name="motif"
                              rows="4" maxlength="1000" required><c:out value="${formMotif}"/></textarea>
                </div>

                <div class="d-flex gap-2">
                    <button class="btn btn-primary" type="submit">Envoyer</button>
                    <a class="btn btn-outline-secondary"
                       href="${pageContext.request.contextPath}/employee/leave-requests">
                        Annuler
                    </a>
                </div>
            </form>
        </div>
    </div>
</main>
</body>
</html>
