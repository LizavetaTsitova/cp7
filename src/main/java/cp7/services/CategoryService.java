package cp7.services;

import cp7.entities.Categories;
import cp7.entities.Companies;
import cp7.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public boolean addCategory(Categories category, Integer compID) {
        category.setCompanyId(compID);

        categoryRepository.save(category);
        return true;
    }

    public void deleteCategory(Integer id){
        Categories category = categoryRepository.findByCategoryId(id);

        categoryRepository.delete(category);
    }
}
