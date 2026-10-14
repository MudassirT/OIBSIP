const express = require('express');
const { getAllPizzas, getInventoryOptions } = require('../controllers/catalogueController');

const router = express.Router();

router.get('/pizzas', getAllPizzas);
router.get('/inventory/options', getInventoryOptions);

module.exports = router;
