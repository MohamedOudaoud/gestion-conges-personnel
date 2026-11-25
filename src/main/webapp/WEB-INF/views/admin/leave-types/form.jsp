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
                    <input type="hidden" name="id" value="${typeConge.id}">
                </c:if>

                <div class="mb-3">
                    <label class="form-label" for="libelle">Libellé</label>
                    <input class="form-control" id="libelle" name="libelle"
                           value="<c:out value='${formLibelle}'/>"
                           maxlength="255" required>
                </div>

                <div class="mb-3">
                    <label class="form-label" for="joursParAn">Jours par an</label>
                    <input class="form-control" id="joursParAn" name="joursParAn"
                           type="number" min="0"
                           value="<c:out value='${formJoursParAn}'/>"
                           aria-describedby="joursParAnHelp">
                    <div class="form-text" id="joursParAnHelp">
                        Obligatoire uniquement si le type décompte le solde.
                    </div>
                </div>

                <div class="form-check mb-3">
                    <input class="form-check-input" id="decompteSolde"
                           name="decompteSolde" type="checkbox" value="true"
                           <c:if test="${formDecompteSolde}">checked</c:if>>
                    <label class="form-check-label" for="decompteSolde">
                        Décompter ce congé du solde
                    </label>
                </div>

                <div class="form-check mb-4">
                    <input class="form-check-input" id="justificatifRequis"
                           name="justificatifRequis" type="checkbox" value="true"
                           <c:if test="${formJustificatifRequis}">checked</c:if>>
                    <label class="form-check-label" for="justificatifRequis">
                        Justificatif obligatoire
                    </label>
                </div>

                <div class="d-flex gap-2">
                    <button class="btn btn-primary" type="submit">Enregistrer</button>
                    <a class="btn btn-outline-secondary"
                       href="${pageContext.request.contextPath}/admin/leave-types">
                        Annuler
                    </a>
                </div>
            </form>
        </div>
    </div>
</main>
</body>
</html>
