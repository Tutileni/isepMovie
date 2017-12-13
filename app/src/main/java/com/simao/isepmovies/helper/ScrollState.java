package com.simao.isepmovies.helper;

/**
 * Constantes qui indiquent l'état de défilement des widgets défilables.
 */
public enum ScrollState {
    /**
     * Le widget est arrêté.
     * Cet état ne signifie pas toujours que ce widget n'a jamais été défilé.
     */
    STOP,

    /**
     * Widget est défilé en le balayant vers le bas.
     */
    UP,

    /**
     * Widget défile en le balayant vers le haut.
     */
    DOWN,
}