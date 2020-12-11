package com.jpoole.service_novigrad;

import android.util.Log;

import org.junit.Test;
import static org.junit.Assert.*;

public class Tests {
    @Test
    public void admin_Deletable() {
        Administrator admin = new Administrator();
        assertFalse(admin.isDeletable());
    }

    @Test
    public void employee_Deletable() {
        Employee employee   = new Employee();
        assertTrue( employee.isDeletable());
    }

    @Test
    public void test_branch() {
        Branch branch   = new Branch();
        branch.setId("number1");
        assertEquals("number1", branch.getId() );
    }

    @Test
    public void test_service() {
        Service service   = new Service();
        service.setPrice(100);
        assertTrue(service.getPrice()== 100.0);
    }

    @Test
    public void test_admin_role() {
        Administrator admin   = new Administrator();
        assertEquals("Administrator",admin.getRoleName() );
    }

    @Test
    public void test_employee_role() {
        Employee employee   = new Employee();
        assertEquals("Employee",employee.getRoleName() );
    }

    @Test
    public void test_customer_role() {
        Customer customer   = new Customer();
        assertEquals("Customer",customer.getRoleName() );
    }

    @Test
    public void test_user_employee_role() {
        User user   = new Employee();
        assertEquals("Employee", user.getRoleName() );
    }

    @Test
    public void test_validate_1() {
        RequestElement request = new RequestElement();
        request.setMandatory(false);
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_2() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_3() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setValidationType("raw");
        request.setData("hello");
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_4() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setValidationType("raw");
        request.setData("");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_5() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("email");
        request.setData("heyThere");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_6() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("email");
        request.setData("heyThere@");
        assertFalse(request.validate());
    }
    @Test
    public void test_validate_7() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("email");
        request.setData("heyThere@.co");
        assertFalse(request.validate());
    }
    @Test
    public void test_validate_8() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("email");
        request.setData("heyThere@c.co");
        assertTrue(request.validate());
    }
    @Test
    public void test_validate_9() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("email");
        request.setData("@c.co");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_10() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("password");
        request.setData("hello123");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_11() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("password");
        request.setData("Hello123");
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_12() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("password");
        request.setData("Hello12");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_13() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("password");
        request.setData("Hello12+");
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_14() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("postalcode");
        request.setData("e3b 6j8");
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_15() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("postalcode");
        request.setData("E3b 6J8");
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_16() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("postalcode");
        request.setData("E 3b6J8");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_17() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("postalcode");
        request.setData("E3b6J8");
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_18() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("postalcode");
        request.setData("63b6J8");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_19() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("address");
        request.setData("??? sh6*");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_20() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("text");
        request.setValidationType("address");
        request.setData("Laurier Ave. 261 Apt#12");
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_21() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("date");
        request.setValidationType("past");
        request.setData("02/14/2000");
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_22() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("date");
        request.setValidationType("future");
        request.setData("02/14/2000");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_23() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("number");
        request.setValidationType("phone");
        request.setData("123 456 789");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_24() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("number");
        request.setValidationType("phone");
        request.setData("123-456-7899");
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_25() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("number");
        request.setValidationType("phone");
        request.setData("123");
        assertFalse(request.validate());
    }

    @Test
    public void test_validate_26() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("number");
        request.setValidationType("age");
        request.setData("123");
        assertTrue(request.validate());
    }

    @Test
    public void test_validate_27() {
        RequestElement request = new RequestElement();
        request.setMandatory(true);
        request.setType("number");
        request.setValidationType("age");
        request.setData("-12");
        assertFalse(request.validate());
    }
}
