package com.tictactoe;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession currentSession = req.getSession();

        // Получаем игровое поле и нажатую ячейку.
        Field field = extractField(currentSession);
        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);

        // Если ячейка уже занята, то возвращаем ту же страницу без изменений параметров сессии.
        if (currentSign != Sign.EMPTY) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        // Ставим крестик.
        field.getField().put(index, Sign.CROSS);
        if (checkWin(resp, currentSession, field)) {
            return;
        }

        // Получаем пустую ячейку.
        int emptyFieldIndex = field.getEmptyFieldIndex();

        // Если такая ячейка существует, то записываем в нее нолик. Иначе ничья.
        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(resp, currentSession, field)) {
                return;
            }
        } else {
            currentSession.setAttribute("draw", true);
            List<Sign> data = field.getFieldData();
            currentSession.setAttribute("data", data);
            resp.sendRedirect("/index.jsp");
            return;
        }

        // Обновляем параметры сессии.
        List<Sign> data = field.getFieldData();
        currentSession.setAttribute("field", field);
        currentSession.setAttribute("data", data);

        resp.sendRedirect("/index.jsp");
    }

    private Field extractField(HttpSession session) {
        Object field = session.getAttribute("field");
        if (field.getClass() != Field.class) {
            session.invalidate();
            throw new RuntimeException("Session is broken, try one more time!");
        }
        return (Field) field;
    }

    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private boolean checkWin(HttpServletResponse resp, HttpSession session, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (winner == Sign.CROSS || winner == Sign.NOUGHT) {
            session.setAttribute("winner", winner);
            List<Sign> data = field.getFieldData();
            session.setAttribute("data", data);
            resp.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }
}
