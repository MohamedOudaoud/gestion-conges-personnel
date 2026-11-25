<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Équipe - <c:out value="${departement.nom}"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <h1 class="h2 mb-4">
        Équipe du département <c:out value="${departement.nom}"/>
    </h1>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>
    <c:if test="${param.status eq 'assigned' or param.status eq 'unassigned'}">
        <div class="alert alert-success" role="status">
            <c:choose>
                <c:when test="${param.status eq 'assigned'}">
                    Affectation du responsable enregistrée.
                </c:when>
                <c:otherwise>Affectation du responsable retirée.</c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <section class="card card-body shadow-sm mb-4">
        <h2 class="h5">Affecter un responsable aux employés sélectionnés</h2>
        <p class="text-muted">
            Seuls les responsables actifs de ce département sont proposés.
            La sélection d'employés remplace leur responsable actuel.
        </p>
        <form action="${pageContext.request.contextPath}/admin/departments/assign-manager"
              method="post">
            <input type="hidden" name="id" value="${departmentId}">
            <div class="mb-3">
                <label class="form-label" for="managerId">Responsable</label>
                <select class="form-select" id="managerId" name="managerId" required>
                    <option value="">-- Choisir un responsable --</option>
                    <c:forEach items="${responsables}" var="responsable">
                        <option value="${responsable.id}"
                                <c:if test="${responsable.id.toString() eq selectedManagerId}">selected</c:if>>
                            <c:out value="${responsable.prenom}"/>
                            <c:out value="${responsable.nom}"/>
                        </option>
                    </c:forEach>
                </select>
                <c:if test="${empty responsables}">
                    <div class="form-text text-danger">
                        Aucun responsable actif n'est enregistré dans ce département.
                    </div>
                </c:if>
            </div>
            <div class="table-responsive">
                <table class="table table-sm table-bordered bg-white">
                    <thead>
                    <tr>
                        <th scope="col">Sélection</th>
                        <th scope="col">Employé</th>
                        <th scope="col">Rôle</th>
                        <th scope="col">Responsable actuel</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${employes}" var="employe">
                        <c:if test="${employe.role ne 'ADMIN'}">
                            <tr>
                                <td>
                                    <input class="form-check-input" type="checkbox"
                                           name="employeeIds" value="${employe.id}"
                                           aria-label="Sélectionner l'employé">
                                </td>
                                <td>
                                    <c:out value="${employe.prenom}"/>
                                    <c:out value="${employe.nom}"/>
                                </td>
                                <td><c:out value="${employe.role}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty employe.manager}">
                                            <c:out value="${employe.manager.prenom}"/>
                                            <c:out value="${employe.manager.nom}"/>
                                        </c:when>
                                        <c:otherwise>Non affecté</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    <c:if test="${empty employes}">
                        <tr>
                            <td colspan="4" class="text-center">
                                Aucun employé dans ce département.
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
            <button class="btn btn-primary" type="submit"
                    <c:if test="${empty responsables}">disabled</c:if>>
                Affecter le responsable aux employés sélectionnés
            </button>
        </form>
    </section>

    <section class="card card-body shadow-sm mb-4">
        <h2 class="h5">Retirer une affectation existante</h2>
        <form action="${pageContext.request.contextPath}/admin/departments/unassign-manager"
              method="post">
            <input type="hidden" name="id" value="${departmentId}">
            <div class="table-responsive">
                <table class="table table-sm table-bordered bg-white">
                    <thead>
                    <tr>
                        <th scope="col">Sélection</th>
                        <th scope="col">Employé</th>
                        <th scope="col">Responsable actuel</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:set var="assignedCount" value="0"/>
                    <c:forEach items="${employes}" var="employe">
                        <c:if test="${not empty employe.manager}">
                            <c:set var="assignedCount" value="${assignedCount + 1}"/>
                            <tr>
                                <td>
                                    <input class="form-check-input" type="checkbox"
                                           name="employeeIds" value="${employe.id}"
                                           aria-label="Sélectionner l'employé">
                                </td>
                                <td>
                                    <c:out value="${employe.prenom}"/>
                                    <c:out value="${employe.nom}"/>
                                </td>
                                <td>
                                    <c:out value="${employe.manager.prenom}"/>
                                    <c:out value="${employe.manager.nom}"/>
                                </td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    <c:if test="${assignedCount eq 0}">
                        <tr>
                            <td colspan="3" class="text-center">
                                Aucune affectation à retirer.
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
            <button class="btn btn-outline-secondary" type="submit"
                    <c:if test="${assignedCount eq 0}">disabled</c:if>>
                Retirer le responsable des employés sélectionnés
            </button>
        </form>
    </section>

    <a class="btn btn-link px-0"
       href="${pageContext.request.contextPath}/admin/departments">
        Retour aux départements
    </a>
</main>
</body>
</html>
