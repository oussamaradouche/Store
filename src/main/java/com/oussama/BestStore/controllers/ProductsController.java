package com.oussama.BestStore.controllers;

import java.io.InputStream;
import java.nio.file.*;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.oussama.BestStore.services.ProductsRepository;

import jakarta.validation.Valid;

import com.oussama.BestStore.models.Product;
import com.oussama.BestStore.models.ProductDTO;

@Controller
@RequestMapping("/products")

public class ProductsController {

    @Autowired
    private ProductsRepository repo;
    
    @GetMapping ({"","/"})
    public String showProductList(Model model){
        List<Product> products=repo.findAll();
        model.addAttribute("products",products);
        return "products/index";
    }
    @GetMapping ("/create")
    public String showCreatePage(Model model){
        ProductDTO productDTO=new ProductDTO();
        model.addAttribute("productDTO",productDTO);
        return "products/createProduct";
    }
    @PostMapping ("/create")
    public String CreateProduct(
        @Valid @ModelAttribute ProductDTO productDTO,BindingResult result      
    ){
        if (productDTO.getImageFile().isEmpty()){
            result.addError(new FieldError("ProductDTO", "imageFile", "the image file is required"));
        }
        if (result.hasErrors()) {
            return "products/createProduct";
        }

        //save image
        MultipartFile image =productDTO.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime()+"_"+image.getOriginalFilename();
        try{
            String uploadDir ="public/images/";
            Path uploadPath=Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try(InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream,Paths.get(uploadDir + storageFileName),StandardCopyOption.REPLACE_EXISTING);
            }
        }catch(Exception e){
            System.out.println("Exception:"+e.getMessage());
        }
        Product product=new Product();
        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setCategory(productDTO.getCategory());
        product.setPrice(productDTO.getPrice());
        product.setDescritpion(productDTO.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);
        repo.save(product);
        return "redirect:/products"; //redirection to the products page in case everything went well 
    }

     @GetMapping("/edit")
     public String showEditPage(
        Model model,        
        @RequestParam int id   // id is the product that we want to edit's id 
     ){
        try {
            Product product =repo.findById(id).get();
            model.addAttribute("product", product);
            ProductDTO productDTO = new ProductDTO();
            productDTO.setName(product.getName());
            productDTO.setBrand(product.getBrand());
            productDTO.setCategory(product.getCategory());
            productDTO.setPrice(product.getPrice());
            productDTO.setDescription(product.getDescritpion());
            model.addAttribute("productDTO", productDTO);
        } catch (Exception e) {
           System.out.println("exception"+e.getMessage());
           return "redirect:/products"; // redirection to the product page if an error happens
        }
        return "products/EditProduct"; 
     }

     @PostMapping("/edit")
     public String updateProduct(Model model,@RequestParam int id,@Valid @ModelAttribute ProductDTO productDTO,BindingResult result){
        try {
            Product product=repo.findById(id).get();
            model.addAttribute("product", product);
            if(result.hasErrors()){
                return "products/editProduct";
            }
            if(!productDTO.getImageFile().isEmpty()){
                //delete old images
                String uploadDir="public/images/";
                Path oldImagePath=Paths.get(uploadDir,product.getImageFileName());
                try {
                    Files.delete(oldImagePath);
                } catch (Exception e) {
                    System.out.println("Exception:"+e.getMessage());
                }
                //save new image file
                MultipartFile image =productDTO.getImageFile();
                Date createdAt=new Date();
                String storageFileName=createdAt.getTime()+"_"+image.getOriginalFilename();
                try(InputStream inputStream=image.getInputStream()){
                    Files.copy(inputStream,Paths.get(uploadDir+storageFileName),StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFileName);
            }
            product.setName(productDTO.getName());
            product.setBrand(productDTO.getBrand());
            product.setCategory(productDTO.getCategory());
            product.setPrice(productDTO.getPrice());
            product.setDescritpion(productDTO.getDescription());

            repo.save(product);
        } catch (Exception e) {
            System.out.println("Exception:"+e.getMessage());
        }
        return "redirect:/products";
     }

     @GetMapping("/delete")
     public String deleteProduct(@RequestParam int id){
        try {
            Product product =repo.findById(id).get();

            //delete image
            Path imagePath=Paths.get("public/images/",product.getImageFileName());
            try {
                Files.delete(imagePath);
            } catch (Exception e) {
                System.out.println("Exception:"+e.getMessage());
            }

            //delete product
            repo.delete(product);

        } catch (Exception e) {
            System.out.println("Exception:"+e.getMessage());
        }
        return "redirect:/products";
     }
    }

