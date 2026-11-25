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
                    <input type="hidden" name="id" value="${departement.id}">
                </c:if>

                <div class="mb-3">
                    <label class="form-label" for="nom">Nom</label>
                    <input class="form-control" id="nom" name="nom"
                           value="<c:out value='${not empty param.nom ? param.nom : departement.nom}'/>"
                           maxlength="255" required>
                </div>

                <div class="mb-4">
                    <label class="form-label" for="description">Description</label>
                    <textarea class="form-control" id="description"
                              name="description" rows="4" maxlength="255"><c:out value="${not empty param.description ? param.description : departement.description}"/></textarea>
                </div>

                <div class="d-flex gap-2">
                    <button class="btn btn-primary" type="submit">Enregistrer</button>
                    <a class="btn btn-outline-secondary"
                       href="${pageContext.request.contextPath}/admin/departments">
                        Annuler
                    </a>
                </div>
            </form>
        </div>
    </div>
</main>
</body>
</html>
