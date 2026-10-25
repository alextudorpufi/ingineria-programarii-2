# Codebase Inspection & Anti-Duplication Guide

To prevent code duplication across the Intelligent Services Marketplace codebase, adhere to this search-first workflow.

---

## 1. Inspection Protocol Before Writing Code

### Step 1: Identify What You Are About to Build
Define the conceptual responsibility:
- Is it a model/entity (e.g. `User`, `Lead`, `Transaction`)?
- Is it a business calculation (e.g. fee calculation, credit check, matching score)?
- Is it an API controller endpoint or route?
- Is it a UI component or form control?
- Is it a utility (e.g. date formatting, currency formatting, validation)?

### Step 2: Query the Codebase
Run targeted search queries before creating a new file:

```bash
# Check if a model, entity, or class exists:
find backend/src -name "*Lead*" -o -name "*Transaction*"
find frontend/app -name "*lead*" -o -name "*card*"

# Search for existing methods or function signatures:
git grep -i "calculateScore"
git grep -i "deductCredits"
git grep -i "formatCurrency"

# Search in mock-ui for reference components and models:
find mock-ui/src -name "*Model*" -o -name "*Service*"
```

### Step 3: Evaluate Existing Components
1. **Exact Match**: If a class or function already handles the exact responsibility, import and call it directly.
2. **Partial Match (80% fit)**: If an existing class or function accomplishes most of the goal, extend or overload it with backward-compatible parameters rather than creating a second version.
3. **Cross-Layer Duplication**: Avoid having duplicate validation rules between frontend and backend. Ensure frontend types and validation schemas closely mirror backend DTO validation annotations.

---

## 2. Common Duplication Traps to Avoid

| Pitfall | Wrong Approach | Clean / DRY Approach |
| :--- | :--- | :--- |
| **Credit Balance Checks** | Writing balance deduction logic in multiple controllers/services. | Centralize in `CreditTransactionService.deductCredits(proId, leadId, amount)` with `@Transactional` protection. |
| **Formatters** | Re-implementing Romanian currency (`RON`) and date formatting in every React page. | Create and reuse a single `formatters.ts` utility or hook. |
| **Status Enums** | Hardcoding status strings (`'PENDING'`, `'MATCHED'`, `'UNLOCKED'`) in multiple places. | Define a single Java enum and TypeScript union type. |
| **UI Inputs / Buttons** | Re-creating custom `<button>` or `<input>` tags in each page. | Import and use `<Button>` and `<Input>` governed by `ui-skill`. |
