<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Historique des congés - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <h1 class="h2 mb-4">Historique des demandes de congé</h1>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <c:set var="historyPrefix"
           value="${historyRole eq 'ADMIN' ? 'admin' : (historyRole eq 'RESPONSABLE' ? 'manager' : 'employee')}"/>
    <form class="row g-2 mb-4"
          action="${pageContext.request.contextPath}/${historyPrefix}/leave-history"
          method="get">
        <div class="col-md-3">
            <label class="form-label" for="status">Statut</label>
            <select class="form-select" id="status" name="status">
                <option value="">Tous les statuts</option>
                <c:forEach items="${statuses}" var="status">
                    <option value="${status}"
                            <c:if test="${selectedStatus eq status}">selected</c:if>>
                        <c:out value="${status}"/>
                    </option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-3">
            <label class="form-label" for="typeCongeId">Type de congé</label>
            <select class="form-select" id="typeCongeId" name="typeCongeId">
                <option value="">Tous les types</option>
                <c:forEach items="${typesConge}" var="typeConge">
                    <option value="${typeConge.id}"
                            <c:if test="${selectedTypeId eq typeConge.id}">selected</c:if>>
                        <c:out value="${typeConge.libelle}"/>
                    </option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label" for="dateDebut">Début à partir du</label>
            <input class="form-control" id="dateDebut" name="dateDebut"
                   type="date" value="<c:out value='${selectedDateDebut}'/>">
        </div>
        <div class="col-md-2">
            <label class="form-label" for="dateFin">Fin jusqu'au</label>
            <input class="form-control" id="dateFin" name="dateFin"
                   type="date" value="<c:out value='${selectedDateFin}'/>">
        </div>
        <div class="col-md-2 d-flex align-items-end">
            <button class="btn btn-primary" type="submit">Filtrer</button>
        </div>
    </form>

    <div class="table-responsive">
        <table class="table table-striped table-bordered align-middle bg-white">
            <thead>
            <tr>
                <th scope="col">Employé</th>
                <th scope="col">Type</th>
                <th scope="col">Début</th>
                <th scope="col">Fin</th>
                <th scope="col">Jours ouvrés</th>
                <th scope="col">Statut</th>
                <th scope="col">Décision</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${demandes}" var="demande">
                <tr>
                    <td>
                        <c:out value="${demande.employe.prenom}"/>
                        <c:out value="${demande.employe.nom}"/>
                    </td>
                    <td><c:out value="${demande.typeConge.libelle}"/></td>
                    <td><c:out value="${demande.dateDebut}"/></td>
                    <td><c:out value="${demande.dateFin}"/></td>
                    <td><c:out value="${demande.nbJours ne null ? demande.nbJours : 0}"/></td>
                    <td><c:out value="${demande.statut}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${demande.statut eq 'REFUSEE'}">
                                <c:out value="${demande.commentaireDecision}"/>
                            </c:when>
                            <c:otherwise>—</c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty demandes}">
                <tr>
                    <td colspan="7" class="text-center">
                        Aucun historique correspondant.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <a class="btn btn-link px-0"
       href="${pageContext.request.contextPath}/dashboard">
        Retour au tableau de bord
    </a>
</main>
</body>
</html>
