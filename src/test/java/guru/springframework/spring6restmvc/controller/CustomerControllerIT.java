package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CustomerControllerIT {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerController customerController;

    @Autowired
    CustomerMapper customerMapper;

    @Rollback
    @Transactional
    @Test
    void testListAllEmptyList() {
        customerRepository.deleteAll();
        List<CustomerDTO> dtos = customerController.listAllCustomers();

        assertThat(dtos.size()).isEqualTo(0);
    }

    @Test
    void testListAll() {
        List<CustomerDTO> dtos = customerController.listAllCustomers();

        assertThat(dtos.size()).isEqualTo(3);
    }

    @Test
    void testGetByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            customerController.getCustomerById(UUID.randomUUID());
        });
    }

    @Test
    void testGetById() {
        Customer customer = customerRepository.findAll().get(0);
        CustomerDTO customerDTO = customerController.getCustomerById(customer.getId());
        assertThat(customerDTO).isNotNull();
    }

    @Rollback
    @Transactional
    @Test
    void testSaveNewCustomer() {
        CustomerDTO customerDto = CustomerDTO.builder()
                .name("Customer Test")
                .build();

        ResponseEntity response = customerController.handlePost(customerDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(response.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = response.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);

        assertThat(savedUUID).isNotNull();
    }

    @Rollback
    @Transactional
    @Test
    void testUpdateExistingCustomer() {
        Customer customer = customerRepository.findAll().get(0);
        CustomerDTO customerDTO = customerMapper.customerToCustomerDto(customer);

        customerDTO.setId(null);
        customerDTO.setVersion(null);
        String customerName = "Customer UPDATED";
        customerDTO.setName(customerName);

        ResponseEntity response = customerController.updateCustomerByID(customer.getId(), customerDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Customer customerUpdated = customerRepository.findById(customer.getId()).get();
        assertThat(customerUpdated.getName()).isEqualTo(customerName);
    }

    @Test
    void testUpdateCustomerNotFound() {
        assertThrows(NotFoundException.class, () -> {
            customerController.updateCustomerByID(UUID.randomUUID(), CustomerDTO.builder().build());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteExistingCustomer() {
        Customer customer = customerRepository.findAll().get(0);

        ResponseEntity response = customerController.deleteCustomerById(customer.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(customerRepository.findById(customer.getId())).isEmpty();
    }

    @Test
    void testDeleteCustomerNotFound() {
        assertThrows(NotFoundException.class, () -> {
            customerController.deleteCustomerById(UUID.randomUUID());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testPatchExistingCustomer() {
        Customer customer = customerRepository.findAll().get(0);
        CustomerDTO customerDTO = customerMapper.customerToCustomerDto(customer);

        customerDTO.setId(null);
        customerDTO.setVersion(null);
        String customerName = "Customer UPDATED Partially";
        customerDTO.setName(customerName);

        ResponseEntity response = customerController.patchCustomerById(customer.getId(), customerDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Customer customerUpdated = customerRepository.findById(customer.getId()).get();
        assertThat(customerUpdated.getName()).isEqualTo(customerName);
    }

    @Test
    void testPatchCustomerNotFound() {
        assertThrows(NotFoundException.class, () -> {
            customerController.updateCustomerByID(UUID.randomUUID(), CustomerDTO.builder().build());
        });
    }
}